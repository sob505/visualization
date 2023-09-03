/*
    Sachi Barnaby
    Project 1: Visualization

    This project creates a GUI to visualize times tables using a circle and lines.
    To run it, just run Visualization.main(). You can use each of the sliders to control different parts of the
    visualization (as indicated in text), and you can use the button to stop or start the animation. The animation
    automatically cycles through 72 different colors by changing the hue value in HSB (see findColors()).

 */

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Visualization extends Application {
    private final Canvas canvas = new Canvas(800,500);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private Timeline animation;
    private double multiplicand = 0;
    private int numPoints = 100;
    private double framerate = 1000; // in milliseconds
    private double increment = 1;

    private final List<Color> colors = findColors();
    private int colorIndex = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Visualization");

        // Set up the main circle in the canvas
        int[] offset = {250,50};
        double diameter = 300;
        drawCircle(offset, diameter);

        // Create a slider so user can change which times table is used
        Slider multiplicandSlider = getSlider(0.0,360.0,multiplicand);

        // Create a slider so user can change how many points are on the circle
        Slider pointsSlider = getSlider(0,360,numPoints);

        // Create a slider so user can control number of frames per second
        Slider framerateSlider = getSlider(0, 2000, framerate);
        framerateSlider.setMajorTickUnit(500);

        // Create a slider so user can control how large the increment is per time step
        Slider incrementSlider = getSlider(0,1, increment);
        incrementSlider.setMajorTickUnit(0.1);

        // Create a button so the user can pause the animation
        Button btn = getButton();

        // Update multiplicand whenever its slider changes
        multiplicandSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            multiplicand = multiplicandSlider.getValue();
            updateSlider(btn, offset, diameter);
        });

        // Update numPoints whenever its slider changes
        pointsSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            numPoints = (int) pointsSlider.getValue();
            updateSlider(btn, offset, diameter);
        });

        // Update framerate whenever its slider changes
        framerateSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            framerate = framerateSlider.getValue();
            animation.stop();
            animation.getKeyFrames().set(0, new KeyFrame(Duration.millis(framerate), event ->
                    runAnimation(offset, diameter, increment)));
            animation.play();
        });

        // Update increment whenever its slider changes
        incrementSlider.valueProperty().addListener((observableValue, oldValue, newValue) ->
                increment = incrementSlider.getValue());

        // Create Labels for the sliders
        Label multiplicandLabel = new Label("Change the multiplicand:");
        Label pointsLabel = new Label("Change the number of points around the circle:");
        Label framerateLabel = new Label("Change the framerate:");
        Label incrementLabel = new Label("Change the size of each time step:");

        // Create an animation for the times table visualization
        animation = new Timeline(
                new KeyFrame(Duration.millis(framerate), event -> runAnimation(offset, diameter, increment))
        );
        animation.setCycleCount(Timeline.INDEFINITE);

        // Create a Pane to hold the canvas, slider, and label
        Pane pane = new Pane();
        pane.getChildren().addAll(canvas, multiplicandSlider, multiplicandLabel, pointsSlider, pointsLabel,
                framerateSlider, framerateLabel, incrementSlider, incrementLabel, btn);

        // Position the sliders and labels within the Pane
        positionElements(multiplicandSlider,multiplicandLabel,pointsSlider,pointsLabel,framerateSlider,framerateLabel,
                incrementSlider,incrementLabel,btn);

        // Create a StackPane to overlay the Pane (canvas + slider)
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(pane);

        // Create a scene
        Scene scene = new Scene(stackPane, 800, 600);

        stage.setScene(scene);
        stage.show();

        animation.play();
    }

    // Set up the Pause/Play button
    private Button getButton() {
        Button btn = new Button();
        btn.setText ("Pause");
        btn.setPrefSize(100,50);

        // When the button is pressed, pause or play the animation
        btn.setOnAction(event -> {
            if (Objects.equals(btn.getText(), "Pause")) {
                animation.pause();
                btn.setText("Play");
            } else {
                animation.play();
                btn.setText("Pause");
            }
        });
        return btn;
    }

    // Set up a slider
    private static Slider getSlider(double min, double max, double start) {
        Slider slider = new Slider(min,max,start);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(40.0);

        // Set the preferred sizes of the sliders
        slider.setPrefWidth(400);

        return slider;
    }

    // Create the main circle to work on
    private void drawCircle(int[] offset, double diameter) {
        gc.setStroke(Color.BLACK);
        gc.strokeOval(offset[0], offset[1], diameter, diameter);
    }

    // Get the coordinates for numPoints around a circle with a given radius
    private static double[][] getPoints(int numPoints, int[] offset, final double diameter) {
        double[][] points = new double[numPoints][2];
        double radius = diameter / 2;
        double[] center = {offset[0] + radius, offset[1] + radius};

        // For each point, find the x and y values
        for (int num = 0; num < numPoints; num++) {
            final double angle = Math.toRadians(((double) num / numPoints) * 360d);

            points[num][0] = -(Math.cos(angle) * radius) + center[0];
            points[num][1] = -(Math.sin(angle) * radius) + center[1];
        }
        return points;
    }

    private void drawLines(int numPoints, double multiplicand, double[][] coordinates) {
        // Circle through all the color options in the list of colors
        Color choice = colors.get(colorIndex);
        colorIndex++;
        if(colorIndex >= colors.size()) { colorIndex = 0; }
        gc.setStroke(choice);

        // Then draw the lines between each of the coordinates
        for(int m = 0; m < numPoints; m++) {
            int remainder = (int) ((multiplicand * m) % numPoints);
            gc.strokeLine(coordinates[m][0], coordinates[m][1], coordinates[remainder][0], coordinates[remainder][1]);
        }
    }

    // Generate a list of colors using HSB
    private static List<Color> findColors() {
        List<Color> colors = new ArrayList<>();
        for(int hue = 0; hue < 360; hue += 5) {
            colors.add(Color.hsb(hue,1,1));
        }
        return colors;
    }

    // Position all the sliders, labels, and the button within the frame
    private static void positionElements(Slider ms, Label ml, Slider ps, Label pl, Slider fs, Label fl, Slider is, Label il,
                                  Button btn) {
        ms.setTranslateX(350);
        ms.setTranslateY(550);
        ml.setTranslateX(50);
        ml.setTranslateY(550);

        ps.setTranslateX(350);
        ps.setTranslateY(500);
        pl.setTranslateX(50);
        pl.setTranslateY(500);

        fs.setTranslateX(350);
        fs.setTranslateY(450);
        fl.setTranslateX(50);
        fl.setTranslateY(450);

        is.setTranslateX(350);
        is.setTranslateY(400);
        il.setTranslateX(50);
        il.setTranslateY(400);

        btn.setTranslateX(600);
        btn.setTranslateY(300);
    }

    // Run the animation using the class attributes
    private void runAnimation(int[] offset, double diameter, double increment){
        multiplicand += increment;
        if(multiplicand > 360) { multiplicand = 0; }
        // Create a 2D array to store the coordinates of numPoints points around a circle
        double[][] coordinates = getPoints(numPoints, offset, diameter);
        // Redraw the circle and lines with the changed value
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawCircle(offset, diameter);
        drawLines(numPoints, multiplicand, coordinates);
    }

    // When the slider is moved, change the corresponding value and pause the animation
    private void updateSlider(Button btn, int[] offset, double diameter) {
        animation.pause();
        btn.setText("Play");
        // Create a 2D array to store the coordinates of numPoints points around a circle
        double[][] coordinates = getPoints(numPoints, offset, diameter);
        // Redraw the circle and lines with the changed value
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawCircle(offset, diameter);
        drawLines(numPoints, multiplicand, coordinates);
    }
}