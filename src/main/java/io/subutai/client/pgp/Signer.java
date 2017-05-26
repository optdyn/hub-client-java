package io.subutai.client.pgp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.security.SignatureException;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;


public class Signer
{
    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

    static
    {
        Security.addProvider( provider );
    }

    private Signer()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static boolean verifyClearSign( byte[] message, PGPPublicKeyRing pgpRings )
            throws IOException, PGPException, SignatureException
    {
        ArmoredInputStream aIn = new ArmoredInputStream( new ByteArrayInputStream( message ) );
        ByteArrayOutputStream bout = new ByteArrayOutputStream();


        //
        // write out signed section using the local line separator.
        // note: trailing white space needs to be removed from the end of
        // each line RFC 4880 Section 7.1
        //
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();

        boolean isFirstLineClearText = aIn.isClearText();
        int lookAhead = readInputLine( lineOut, aIn );

        if ( lookAhead != -1 && isFirstLineClearText )
        {
            bout.write( lineOut.toByteArray() );
            while ( lookAhead != -1 && aIn.isClearText() )
            {
                lookAhead = readInputLine( lineOut, lookAhead, aIn );
                bout.write( lineOut.toByteArray() );
            }
        }

        JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory( aIn );
        PGPSignatureList p3 = ( PGPSignatureList ) pgpFact.nextObject();
        PGPSignature sig = p3.get( 0 );


        PGPPublicKey publicKey = pgpRings.getPublicKey( sig.getKeyID() );
        sig.init( new JcaPGPContentVerifierBuilderProvider().setProvider( "BC" ), publicKey );

        //
        // read the input, making sure we ignore the last newline.
        //

        InputStream sigIn = new ByteArrayInputStream( bout.toByteArray() );

        lookAhead = readInputLine( lineOut, sigIn );

        processLine( sig, lineOut.toByteArray() );

        if ( lookAhead != -1 )
        {
            do
            {
                lookAhead = readInputLine( lineOut, lookAhead, sigIn );

                sig.update( ( byte ) '\r' );
                sig.update( ( byte ) '\n' );

                processLine( sig, lineOut.toByteArray() );
            }
            while ( lookAhead != -1 );
        }

        sigIn.close();

        return sig.verify();
    }


    public static byte[] clearSign( byte[] message, PGPSecretKey pgpSecKey, char[] pass, String digestName )
            throws IOException, PGPException, SignatureException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int digest;

        if ( "SHA256".equals( digestName ) )
        {
            digest = PGPUtil.SHA256;
        }
        else if ( "SHA384".equals( digestName ) )
        {
            digest = PGPUtil.SHA384;
        }
        else if ( "SHA512".equals( digestName ) )
        {
            digest = PGPUtil.SHA512;
        }
        else if ( "MD5".equals( digestName ) )
        {
            digest = PGPUtil.MD5;
        }
        else if ( "RIPEMD160".equals( digestName ) )
        {
            digest = PGPUtil.RIPEMD160;
        }
        else
        {
            digest = PGPUtil.SHA1;
        }

        PGPPrivateKey pgpPrivKey =
                pgpSecKey.extractPrivateKey( new JcePBESecretKeyDecryptorBuilder().setProvider( "BC" ).build( pass ) );
        PGPSignatureGenerator sGen = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder( pgpSecKey.getPublicKey().getAlgorithm(), digest ).setProvider( "BC" ) );
        PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

        sGen.init( PGPSignature.CANONICAL_TEXT_DOCUMENT, pgpPrivKey );

        Iterator it = pgpSecKey.getPublicKey().getUserIDs();
        if ( it.hasNext() )
        {
            spGen.setSignerUserID( false, ( String ) it.next() );
            sGen.setHashedSubpackets( spGen.generate() );
        }

        InputStream fIn = new ByteArrayInputStream( message );
        ArmoredOutputStream aOut = new ArmoredOutputStream( out );

        aOut.beginClearText( digest );

        //
        // note the last \n/\r/\r\n in the file is ignored
        //
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        int lookAhead = readInputLine( lineOut, fIn );

        processLine( aOut, sGen, lineOut.toByteArray() );

        if ( lookAhead != -1 )
        {
            do
            {
                lookAhead = readInputLine( lineOut, lookAhead, fIn );

                sGen.update( ( byte ) '\r' );
                sGen.update( ( byte ) '\n' );

                processLine( aOut, sGen, lineOut.toByteArray() );
            }
            while ( lookAhead != -1 );
        }

        fIn.close();

        aOut.endClearText();

        BCPGOutputStream bOut = new BCPGOutputStream( aOut );

        sGen.generate().encode( bOut );

        aOut.close();

        return out.toByteArray();
    }


    private static void processLine( PGPSignature sig, byte[] line ) throws SignatureException, IOException
    {
        int length = getLengthWithoutWhiteSpace( line );
        if ( length > 0 )
        {
            sig.update( line, 0, length );
        }
    }


    private static int readInputLine( ByteArrayOutputStream bOut, InputStream fIn ) throws IOException
    {
        bOut.reset();

        int lookAhead = -1;
        int ch;

        while ( ( ch = fIn.read() ) >= 0 )
        {
            bOut.write( ch );
            if ( ch == '\r' || ch == '\n' )
            {
                lookAhead = readPassedEOL( bOut, ch, fIn );
                break;
            }
        }

        return lookAhead;
    }


    private static int readPassedEOL( ByteArrayOutputStream bOut, int lastCh, InputStream fIn ) throws IOException
    {
        int lookAhead = fIn.read();

        if ( lastCh == '\r' && lookAhead == '\n' )
        {
            bOut.write( lookAhead );
            lookAhead = fIn.read();
        }

        return lookAhead;
    }


    private static int readInputLine( ByteArrayOutputStream bOut, int lookAhead, InputStream fIn ) throws IOException
    {
        bOut.reset();

        int ch = lookAhead;

        do
        {
            bOut.write( ch );
            if ( ch == '\r' || ch == '\n' )
            {
                lookAhead = readPassedEOL( bOut, ch, fIn );
                break;
            }
        }
        while ( ( ch = fIn.read() ) >= 0 );

        if ( ch < 0 )
        {
            lookAhead = -1;
        }

        return lookAhead;
    }


    private static void processLine( OutputStream aOut, PGPSignatureGenerator sGen, byte[] line )
            throws SignatureException, IOException
    {
        // note: trailing white space needs to be removed from the end of
        // each line for signature calculation RFC 4880 Section 7.1
        int length = getLengthWithoutWhiteSpace( line );
        if ( length > 0 )
        {
            sGen.update( line, 0, length );
        }

        aOut.write( line, 0, line.length );
    }


    private static int getLengthWithoutWhiteSpace( byte[] line )
    {
        int end = line.length - 1;

        while ( end >= 0 && isWhiteSpace( line[end] ) )
        {
            end--;
        }

        return end + 1;
    }


    private static boolean isWhiteSpace( byte b )
    {
        return isLineEnding( b ) || b == '\t' || b == ' ';
    }


    private static boolean isLineEnding( byte b )
    {
        return b == '\r' || b == '\n';
    }
}
