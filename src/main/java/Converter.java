import com.sun.deploy.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

public class Converter {


    private final int bitDepth;
    private final Color[] palette;
    private final int bitMask;
    private int bitShift;
    private int pixelsPerByte;
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);



    public static void main(String[] args) throws IOException, URISyntaxException {
        /*Color[] palette = {
                new Color(0, 0, 0),         // MINI_BLACK 0
                new Color(255, 255, 255),   // MINI_WHITE 1
                new Color(0, 0, 128),     // MINI_NAVY 2
                new Color(0, 128, 128),     // MINI_DARKCYAN 3
                new Color(0, 128, 0),     // MINI_DARKGREEN 4
                new Color(128, 0, 0),     // MINI_MAROON 5
                new Color(128, 0, 128),   // MINI_PURPLE 6
                new Color(128, 128, 0),   // MINI_OLIVE 7
                new Color(192, 192, 192),   // MINI_LIGHTGREY 8
                new Color(128, 128, 128),   // MINI_DARKGREY 9
                new Color(0, 0, 255),     // MINI_BLUE 10
                new Color(0, 255, 0),     // MINI_GREEN 11
                new Color(0, 255, 255),     // MINI_CYAN 12
                new Color(255, 0, 0),   // MINI_RED 13
                new Color(255, 0, 255),   // MINI_MAGENTA 14
                new Color(255, 165, 0)   // MINI_ORANGE 15*/
       // };
    Color[] palette = {
            new Color(0, 0, 0),         // MINI_BLACK 0
            new Color(255, 255, 255),   // MINI_WHITE 1
            new Color(255, 179, 30),
            new Color(121, 196, 226)};
            Converter converter = new Converter(1, palette);
            converter.convertImage("Tagi.png");

            /*File dir = new File(Converter.class.getClassLoader().getResource("SquixLogo.png").toURI());

            for (File file : dir.listFiles()){
                if (file.getName().matches(".*.bmp")) {

                    converter.convertImage(file.getName());
                }
            }
        for (File file : dir.listFiles()){
                System.out.println(file.getName().replaceAll("([a-zA-Z0-9_]*).*", "$1"));
        }*/





    }

    public Converter (int bitDepth, Color palette[]) throws URISyntaxException {
        this.bitDepth = bitDepth;
        this.palette = palette;
        this.bitMask = (1 << bitDepth) - 1;
        this.pixelsPerByte = 8 / bitDepth;
        switch(bitDepth) {
            case 1:
                bitShift = 3;
                break;
            case 2:
                bitShift = 2;
                break;
            case 4:
                bitShift = 1;
                break;
            case 8:
                bitShift = 0;
                break;
        }
    }

    public String getHexString(long value, int numBytes) {
        buffer.putLong(0, value);
        byte bytes[] = buffer.array();
        List<String> target = new ArrayList<String>();
        for (int i = 8 - numBytes; i < 8; i++) {
            target.add(String.format("0x%02X", bytes[i]));
        }
        return StringUtils.join(target, ", ");
    }

    public void convertImage(String filename) throws IOException {
        BufferedImage image = ImageIO.read(this.getClass().getClassLoader().getResource(filename));


        int widthRoundedUp = (image.getWidth() + 7) & ~7;
        int[][] palettedImage = new int[widthRoundedUp][image.getHeight()];
        int[] buffer = new int[widthRoundedUp * image.getHeight() / pixelsPerByte];

        int bitCounter = 0;
        int byteCounter = 0;
        System.out.println("Width: " + image.getWidth());
        System.out.println("Height:" + image.getHeight());
        for (int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
            for (int xPixel = 0; xPixel < image.getWidth(); xPixel++) {

                if (bitCounter == pixelsPerByte) {
                    byteCounter ++;
                    bitCounter = 0;
                }
                int color = image.getRGB(xPixel, yPixel);
                int paletteIndex = getNearestPaletteIndex(new Color(color));
                int pos = byteCounter;
                int shift = 8 - (bitCounter + 1) * bitDepth;
                int mask = bitMask;
                int palColor = paletteIndex;
                //System.out.println(xPixel + ", " + yPixel + ": "+ paletteIndex + ", POS: " + pos);
                palColor = palColor << shift;
                //System.out.println(xPixel + ", " + yPixel + ": Pos: " + pos + ", Shift: " + shift + ", Mask: " + mask + ", PalColor: "  + palColor + ", PaletteIndex: " + paletteIndex);
                //buffer[pos] = ((buffer[pos] & ~mask) | (palColor & mask));
                buffer[pos] = buffer[pos] | palColor;
                bitCounter++;
            }
            byteCounter++;
            bitCounter = 0;
        }
        int version = 1;
        if (true) {
            String imageName = filename.replaceAll("([a-zA-Z0-9_]*).*", "$1");
            System.out.println("const char mini" + imageName + "[] PROGMEM = {");
            byte data = 0;

            System.out.println("\t" + getHexString(version, 1) + ", // Version: " + version);
            System.out.println("\t" + getHexString(bitDepth, 1) + ", // BitDepth: " + bitDepth);
            System.out.println("\t" + getHexString(image.getWidth(), 2) + ", // Width: " + image.getWidth());
            System.out.println("\t" + getHexString(image.getHeight(), 2) + ", // Height: " + image.getHeight());
            System.out.print("\t// Round width to next byte: " + widthRoundedUp + "\n\t");
            for (int i = 0; i < widthRoundedUp * image.getHeight() / pixelsPerByte; i++) {

                System.out.print(String.format("0x%02X, ", buffer[i]));
                if (i % 20 == 19) {
                    System.out.print("\n\t");

                }


            }


            System.out.println("};");
        } else {
            FileOutputStream fos = new FileOutputStream(filename + ".bin");
            fos.write(version);
            fos.write(bitDepth);

            fos.write((byte) ((image.getWidth() >> 8) & 0xFF));
            fos.write((byte) (image.getWidth() & 0xFF));
            fos.write((byte) ((image.getHeight() >> 8) & 0xFF));
            fos.write((byte) (image.getHeight() & 0xFF));

            for (int i = 0; i < widthRoundedUp * image.getHeight() / pixelsPerByte; i++) {
                fos.write((byte)buffer[i]);
            }
            fos.close();
        }

    }

    public byte getNearestPaletteIndex(Color color) {
        long minDistance = Long.MAX_VALUE;
        byte minIndex = 0;
        for (byte i = 0; i < Math.pow(2, bitDepth); i++) {
            long distance = getColorDistance(palette[i], color);
            if (distance < minDistance) {

                minDistance = distance;
                minIndex = i;
                /*if (distance == 0) {
                    return i;
                }*/
            }
        }
        return minIndex;
    }

    public long getColorDistance(Color a, Color b) {
        return (long) ( Math.pow(a.getRed() - b.getRed(), 2) + Math.pow(a.getGreen() - b.getGreen(), 2) + Math.pow(a.getBlue() - b.getBlue(), 2));
    }


}