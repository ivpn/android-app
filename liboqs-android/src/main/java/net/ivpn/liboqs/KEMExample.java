package net.ivpn.liboqs;

import java.util.Arrays;

public class KEMExample {

    public static void main(String[] args) {
        System.out.println("Supported KEMs:");
        Common.print_list(KEMs.get_supported_KEMs());
        System.out.println();

        System.out.println("Enabled KEMs:");
        Common.print_list(KEMs.get_enabled_KEMs());
        System.out.println();

        String kem_name = "DEFAULT";
        KeyEncapsulation client = new KeyEncapsulation(kem_name);
        client.print_details();
        System.out.println();

        long t = System.currentTimeMillis();
        byte[] client_public_key = client.generate_keypair();
        long timeElapsed = System.currentTimeMillis() - t;

        System.out.println("Client public key:");
        System.out.println(Common.chop_hex(client_public_key));
        System.out.println("\nIt took " + timeElapsed + " millisecs to generate the key pair.");

        KeyEncapsulation server = new KeyEncapsulation(kem_name);

        t = System.currentTimeMillis();
        Pair<byte[], byte[]> server_pair = server.encap_secret(client_public_key);
        System.out.println("It took " + (System.currentTimeMillis() - t) + " millisecs to encapsulate the secret.");
        byte[] ciphertext = server_pair.getLeft();
        byte[] shared_secret_server = server_pair.getRight();

        t = System.currentTimeMillis();
        byte[] shared_secret_client = client.decap_secret(ciphertext);
        System.out.println("It took " + (System.currentTimeMillis() - t) + " millisecs to decapsulate the secret.");

        client.dispose_KEM();
        server.dispose_KEM();

        System.out.println("\nClient shared secret:");
        System.out.println(Common.chop_hex(shared_secret_client));
        System.out.println("\nServer shared secret:");
        System.out.println(Common.chop_hex(shared_secret_server));

        System.out.println("\nShared secrets coincide? " + Arrays.equals(shared_secret_client, shared_secret_server));
    }

}
