import java.io.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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

        // scale image view to fit on window
        view.setFitWidth(IMAGE_VIEW_WIDTH);
        view.setFitHeight(IMAGE_VIEW_WIDTH);

        view.setPreserveRatio(true);

        // same for padded version
        padView.setFitWidth(IMAGE_VIEW_WIDTH + 100);
        padView.setFitHeight(IMAGE_VIEW_WIDTH + 100);




        // get pixel value at given position
        PixelReader pixelReader = originalImage.getPixelReader();
        int argbValue = pixelReader.getArgb(3000, 100);
        int alpha = (argbValue >> 24) & 0xFF;
        int red = (argbValue >> 16) & 0xFF;
        int green = (argbValue >> 8) & 0xFF;
        int blue = argbValue & 0xFF;
        System.out.println("ARGB at (100, 100): " + alpha + ", " + red + ", " + green + ", " + blue);

        // create new image with either black padding or extend colors
        //      make method for padding

        // create matrix/list object for the filter
        //      maybe constants to choose from

        // iterate over every pixel

        // setup grid object to put image onto
        GridPane root = new GridPane();

        root.add(view, 0, 0);
        root.add(padView, 1, 0);

        Scene scene = new Scene(root, 800, 840);
        stage.setScene(scene);
        stage.show();
    }

    public Image addPadding(Image image) {

        // create blank Writable image and get instance to writer
        WritableImage paddedImage = new WritableImage(IMAGE_WIDTH + 1, IMAGE_HEIGHT + 1);
        PixelWriter writer = paddedImage.getPixelWriter();

        // create reader for passed image
        PixelReader reader = image.getPixelReader();

        // iterate over all pixels
        for (int y = 0; y < IMAGE_HEIGHT + 1; ++y) {
            for (int x = 0; x < IMAGE_WIDTH + 1; ++x) {

                // booleans for determining if pixel is on border
                boolean isInXBorder = x == 0 || x == IMAGE_WIDTH;
                boolean isInYBorder = y == 0 || y == IMAGE_HEIGHT;

                // booleans determining if pixel is in one of the corners
                boolean is00Corner = x == 0 && y == 0;
                boolean is01Corner = x == 0 && y == IMAGE_HEIGHT;
                boolean is10Corner = x == IMAGE_WIDTH && y == IMAGE_HEIGHT;
                boolean is11Corner = x ==IMAGE_WIDTH && y == 0;

                // casting these booleans to string in sequence
                String cornerResult = is00Corner + "-" + is01Corner
                        + "-" + is10Corner + "-" + is11Corner;

                // check if pixel is in the border
                if (isInXBorder || isInYBorder) {

                    /* check if this pixel in a corner, if so which one
                     * and color with the same color as it's diagonal
                     */
                    Color newColor = null;
                    switch (cornerResult) {
                        case "true-false-false-false":
                            newColor = reader.getColor(x, y);
                            writer.setColor(x, y, newColor);
                            break;
                        case "false-true-false-false":
                            newColor = reader.getColor(x, y - 1);
                            writer.setColor(x, y, newColor);
                            break;
                        case "false-false-true-false":
                            newColor = reader.getColor(x - 1, y - 1);
                            writer.setColor(x, y, newColor);
                            break;
                        case "false-false-false-true":
                            newColor = reader.getColor(x - 1, y);
                            writer.setColor(x, y, newColor);
                            break;
                    }

                    // if just general border, get neighbor color
                    //TODO add border padding, then test by grabbing pixel values
                }
            }
        }

        return paddedImage;
    }
}