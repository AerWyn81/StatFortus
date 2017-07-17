package pro.roquelaure.statfortus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Crée le 07/07/2017 par Vincent.
 */

public class InterfaceModbus {

    private int portServeur;
    private String adresseIP;
    private Socket socket;
    private OutputStream socketEcriture;
    private InputStream socketLecture;

    public InterfaceModbus(String ip, int port) {
        adresseIP = ip;
        portServeur = port;
    }

    public int testConnexion() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(adresseIP,portServeur),500);
            socket.close();
            return 0;
        } catch (IOException e) {
            //e.printStackTrace();
            return 1;
        }
    }

    public int setMultipleAddress(int adr, int nbMot, int[] val) {
        byte adrPf = (byte) adr;

        if (nbMot == 4)
        {
            byte[] trameEcriture = {0x00, 0x02, 0x00, 0x00, 0x00, 0x06, 0x01, 0x10, 0x00, adrPf, 0x00, (byte) nbMot, 8, 0x00, (byte) val[0], 0x00, (byte) val[1], 0x00, (byte) val[2], 0x00, (byte) val[3]};
            try {
                socketEcriture.write(trameEcriture);
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }
        }
        else
        {
            byte[] trameEcriture = {0x00, 0x02, 0x00, 0x00, 0x00, 0x06, 0x01, 0x10, 0x00, adrPf, 0x00, (byte) nbMot, 12, 0x00, (byte) val[0], 0x00, (byte) val[1], 0x00, (byte) val[2], 0x00, (byte) val[3], 0x00, (byte) val[4], 0x00, (byte) val[5]};
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(adresseIP,portServeur),3000);
                socketEcriture = socket.getOutputStream();
                socketEcriture.write(trameEcriture);
                socket.close();
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }
        }
    }

    public int setOneAddress(int adr, int valeur) {
        byte adrPf = (byte) adr;
        byte[] trame = {0x00, 0x02, 0x00, 0x00, 0x00, 0x06, 0x01, 0x06, 0x00, adrPf, 0x00, (byte) valeur};
        try {
            Socket socket = new Socket(adresseIP,portServeur);
            socketEcriture = socket.getOutputStream();
            socketEcriture.write(trame);
            Thread.sleep(40);
            //socketEcriture.close();
            socket.close();
            return 0;
        } catch (IOException | InterruptedException e) {
            //e.printStackTrace();
            return 1;
        }
    }

    public int[] getMultiAddress(int adr, int nbMot) {
        byte adrPf = (byte) adr;
        byte[] trameLecture = {0x00, 0x02, 0x00, 0x00, 0x00, 0x06, 0x01, 0x03, 0x00, adrPf, 0x00, (byte) nbMot};

        if (nbMot == 4) {
            byte[] trameRecu = new byte[17];

            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(adresseIP,portServeur),3000);
                socketEcriture = socket.getOutputStream();
                socketLecture = socket.getInputStream();
                socketEcriture.write(trameLecture);
                socketLecture.read(trameRecu);
                socket.close();

                int[] i = new int[4];
                i[0] =  trameRecu[10] & 0xFF;
                i[1] =  trameRecu[12] & 0xFF;
                i[2] =  trameRecu[14] & 0xFF;
                i[3] =  trameRecu[16] & 0xFF;

                return i;
            } catch (IOException e) {
                e.printStackTrace();
                return new int[]{1};
            }
        }
        else {
            byte[] trameRecu = new byte[21];

            try {
                Socket socket = new Socket();
                socketEcriture = socket.getOutputStream();
                socketLecture = socket.getInputStream();
                socketEcriture.write(trameLecture);
                socketLecture.read(trameRecu);
                socket.close();

                int[] i = new int[6];
                i[0] = trameRecu[10] & 0xFF;
                i[1] = trameRecu[12] & 0xFF;
                i[2] = trameRecu[14] & 0xFF;
                i[3] = trameRecu[16] & 0xFF;
                i[4] = trameRecu[18] & 0xFF;
                i[5] = trameRecu[20] & 0xFF;

                return i;
            } catch (IOException e) {
                e.printStackTrace();
                return new int[]{1};
            }
        }
    }

    public int getOneAddress(int adr) {
        byte adrPf = (byte) adr;
        byte[] trameLecture = {0x00, 0x02, 0x00, 0x00, 0x00, 0x06, 0x01, 0x03, 0x00, adrPf, 0x00, 0x01};
        byte[] trameRecu = new byte[11];

        try {
            Socket socket = new Socket(adresseIP,portServeur);
            socketEcriture = socket.getOutputStream();
            socketLecture = socket.getInputStream();
            socketEcriture.write(trameLecture);
            Thread.sleep(40);
            socketLecture.read(trameRecu);
            Thread.sleep(40);
            //socketEcriture.close();
            //socketLecture.close();
            socket.close();
            return trameRecu[10] & 0xFF;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 999;
        }
    }
}
