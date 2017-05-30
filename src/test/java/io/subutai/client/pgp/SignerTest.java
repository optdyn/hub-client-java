package io.subutai.client.pgp;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.StringTokenizer;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import static junit.framework.Assert.assertEquals;


public class SignerTest
{

    private static final String UTF8 = "UTF-8";


    public static InputStream getKeyFileAsStream()
    {
        return SignerTest.class.getClassLoader().getResourceAsStream( "test-keys" );
    }


    @Test
    public void testSignNVerify() throws Exception
    {
        String theKeys = IOUtils.toString( getKeyFileAsStream(), UTF8 );

        InputStream secretKeyStream = new ByteArrayInputStream( getKeyBlock( theKeys, true ).getBytes( UTF8 ) );

        PGPSecretKeyRingCollection secretKeyRingCollection =
                new PGPSecretKeyRingCollection( PGPUtil.getDecoderStream( secretKeyStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPSecretKeyRing secretKeyRing = secretKeyRingCollection
                .getSecretKeyRing( secretKeyRingCollection.iterator().next().getPublicKey().getKeyID() );

        PGPSecretKey secondSecretKey = secretKeyRing.getSecretKey();


        byte[] signedMessageArmor =
                //new line after message is crucial since without it signing fails
                Signer.clearSign( "test\n".getBytes( UTF8 ), secondSecretKey, "".toCharArray(), "" );

        String signedMessage = new String( signedMessageArmor, UTF8 );

        System.out.println( signedMessage );

        InputStream publicKeyStream = new ByteArrayInputStream( getKeyBlock( theKeys, false ).getBytes( UTF8 ) );

        PGPPublicKeyRingCollection publicKeyRingCollection =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( publicKeyStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPPublicKeyRing pgpKeyring = publicKeyRingCollection
                .getPublicKeyRing( publicKeyRingCollection.iterator().next().getPublicKey().getKeyID() );

        boolean result = Signer.verifyClearSign( signedMessage.getBytes(), pgpKeyring );

        if ( result )
        {
            System.out.println( "signature verified." );
        }
        else
        {
            System.out.println( "signature verification failed." );
        }

        assertEquals( true, result );
    }


    private String getKeyBlock( String keys, boolean privateBlock )
    {
        StringTokenizer lineSplitter = new StringTokenizer( keys, "\n" );

        StringBuilder keyBuffer = new StringBuilder();
        boolean append = false;

        while ( lineSplitter.hasMoreTokens() )
        {
            String nextLine = lineSplitter.nextToken();

            if ( nextLine.contains(
                    String.format( "-----BEGIN PGP %s KEY BLOCK-----", privateBlock ? "PRIVATE" : "PUBLIC" ) ) )
            {
                append = true;
            }

            if ( append )
            {
                keyBuffer.append( nextLine );
            }

            if ( nextLine.contains(
                    String.format( "-----END PGP %s KEY BLOCK-----", privateBlock ? "PRIVATE" : "PUBLIC" ) ) )
            {
                break;
            }
        }

        return keyBuffer.toString();
    }
}
