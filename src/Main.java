import java.io.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.*;

public class Main extends Application{

    /** path to image file */
    public static String IMAGE_PATH =
            "C:\\Users\\arngm\\Documents\\" +
                    "PersonalProjects\\edgeDetection\\image.jpg";

    /** Width of original image */
    public static int IMAGE_WIDTH = 3024;

    /** height of original image */
    public static int IMAGE_HEIGHT = 4032;

    /** Width of image view*/
    public static int IMAGE_VIEW_WIDTH = 500;

    public static void main(String[] args) {
        launch();
        System.out.println("Hello world!");
    }

    @Override
    public void start(Stage stage) throws Exception {

        // read image file
        File file;
        try {
            // read image
            file = new File(IMAGE_PATH);


            //originalImage = ImageIO.read(inputImage);
        } catch (Exception e) {

            System.out.println("Error: The image file is not in the working directory");
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            return;

        }
        // pass image into javafx image view
        Image originalImage = new Image(file.toURI().toString());
        ImageView view = new ImageView(originalImage);

        Image paddedVersion = addPadding(originalImage);
        ImageView padView = new ImageView(paddedVersion);

        Image smallTestImage = blankSmallImage();
        ImageView testImage = new ImageView(smallTestImage);
        ImageView padTest = new ImageView(addPadding(smallTestImage));

        // scale image view to fit on window
        view.setFitWidth(IMAGE_VIEW_WIDTH);
        view.setFitHeight(IMAGE_VIEW_WIDTH);
        view.setPreserveRatio(true);

        testImage.setFitHeight(100);
        testImage.setPreserveRatio(true);
        testImage.setSmooth(false);

        padTest.setFitHeight(100);
        padTest.setPreserveRatio(true);
        padTest.setSmooth(false);

        // same for padded version
        padView.setFitWidth(IMAGE_VIEW_WIDTH + 100);
        padView.setFitHeight(IMAGE_VIEW_WIDTH + 100);
        padView.setPreserveRatio(true);

        // create matrix/list object for the filter

        int[][] verticalKernelFilter = new int[][]{new int[]{-1, 0, 1},
                                                    new int[]{-2, 0, 2},
                                                    new int[]{-1, 0, 1}};

        int[][] horizontalKernelFilter = new int[][]{new int[]{-1, -2, -1},
                new int[]{-0, 0, 0},
                new int[]{1, 2, 1}};

        // create 2d array to store intermediate cross correlation values
        int[][] crossCorrelationValues = new int[IMAGE_HEIGHT][IMAGE_WIDTH];

        // setup grid object to put image onto
        GridPane root = new GridPane();

        root.add(view, 0, 0);
        root.add(padView, 0, 1);

        root.add(testImage, 1, 0);
        root.add(padTest, 1, 1);

        Scene scene = new Scene(root, 800, 840);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * This method is responsible for adding a layer of padding to the outside
     * of the image, 1 pixel thick. A 1920x1080 image will come out as a
     * 1921x1081. The color of the padding will match the nearest pixel as to
     * extend the image
     * @param image Image object to pad
     * @return Image passed with padding
     */
    public Image addPadding(Image image) {

        double width = image.getWidth();
        double height = image.getHeight();

        // create blank Writable image and get instance to writer
        WritableImage paddedImage = new WritableImage((int) width + 2, (int) height + 2);
        PixelWriter writer = paddedImage.getPixelWriter();

        double newWidth = paddedImage.getWidth();
        double newHeight = paddedImage.getHeight();

        System.out.println("old width = " + width + " - new width = " + newWidth);
        System.out.println("old height = " + height + " - new height = " + newHeight);

        // create reader for passed image
        PixelReader reader = image.getPixelReader();

        for (int y = 0; y < paddedImage.getHeight(); ++y) {
            for (int x = 0; x < paddedImage.getWidth(); ++x) {

                boolean isInTopLeft = (x == 0 && (y == 0 || y == 1)) ||
                        (x == 1 && y == 0);
                boolean isInBotLeft = (x == newWidth - 2 &&
                        (y == 0 || y == 1)) ||
                        (x == newWidth - 2 && y == 0);
                boolean isInTopRight = (x == 0 &&
                            (y == newHeight - 2  || y == newHeight - 1)) ||
                        (x == 1 && y == newHeight - 1);
                boolean isInBotRight = (x == newWidth - 1 &&
                            (y == newHeight - 1 || y == newHeight - 2)) ||
                        (x == newWidth - 2 && y == newHeight - 1);

                boolean isInXBorder = x == 0 || x == newWidth - 1;
                boolean isInYBorder = y == 0 || y == newHeight - 1;

                if (isInTopLeft) {
                    Color color = reader.getColor(0, 0);

                    writer.setColor(0, 0, color);
                    writer.setColor(0, 1, color);
                    writer.setColor(1, 0, color);

                } else if (isInBotLeft) {
                    Color color = reader.getColor(0, (int) height - 1);

                    writer.setColor(0, (int) newHeight - 1, color);
                    writer.setColor(0, (int) newHeight - 2, color);
                    writer.setColor(1, (int) newHeight - 1, color);
                } else if (isInBotRight) {
                    Color color = reader.getColor((int) width - 1, (int) height - 1);

                    writer.setColor((int) newWidth - 1, (int) newHeight - 1, color);
                    writer.setColor((int) newWidth - 1, (int) newHeight - 2, color);
                    writer.setColor((int) newWidth - 2, (int) newHeight - 1, color);
                } else if (isInTopRight) {
                    Color color = reader.getColor((int) width - 1, 0);

                    writer.setColor((int) newWidth - 1, 0, color);
                    writer.setColor((int) newWidth - 1, 1, color);
                    writer.setColor((int) newWidth - 2, 0, color);
                }
                // fill in middle of image
                if (!isInXBorder && !isInYBorder) {
                    Color color = reader.getColor(x - 1, y - 1);

                    writer.setColor(x, y, color);
                }
            }
        }
        // writing top and bottom border
        for (int x = 2; x < paddedImage.getWidth() - 2; ++x) {
            Color color = reader.getColor(x - 1, 0);
            Color color2 = reader.getColor(x - 1, (int) height - 1);

            writer.setColor(x, 0, color);
            writer.setColor(x, (int) newHeight - 1, color2);
        }

        // writing left and right border
        for (int y = 2; y < paddedImage.getHeight() - 2; ++y) {
            Color color = reader.getColor(0, y - 1);
            Color color2 = reader.getColor((int) width - 1, y - 1);

            writer.setColor(0, y, color);
            writer.setColor((int) newWidth - 1, y, color2);
        }


        return paddedImage;
    }

    /**
     * This method is responsible for creating a small image of 10x10 pixels
     * with alternating colors, used to test if the padding
     * @return a 10 x 10 image with
     */
    public Image blankSmallImage() {
        WritableImage smallImage = new WritableImage(10, 10);
        PixelWriter writer = smallImage.getPixelWriter();
        for (int y = 0; y < 10; ++y) {
            for (int x = 0; x < 10; ++x) {
                if (x % 2 == 1 && y % 2 == 1) {
                    writer.setColor(x, y, Color.YELLOW);
                } else {
                    writer.setColor(x, y, Color.BLACK);
                }
            }
        }
        return smallImage;
    }
}