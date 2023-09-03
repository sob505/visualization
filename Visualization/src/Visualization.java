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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Visualization extends Application {
    private final Canvas canvas = new Canvas(800,500);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private Timeline animation;
    private double step = 1;
    private double oldMultiplicand = 2;
    private double multiplicand = 2;
    private int numPoints = 100;
    private double framerate = 1000; // in milliseconds
    private double increment = 1;

    private final List<Color> colors = findColors(); //allColors();
    private int colorIndex = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Visualization");

        int[] offset = {250,50};
        double diameter = 300;
        drawCircle(offset, diameter);


        // Create a slider so user can change which times table is used
        Slider multiplicandSlider = getSlider(0.0,360.0,0);

        // Create a slider so user can change how many points are on the circle
        Slider pointsSlider = getSlider(0,360,0);

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

        // Set the preferred sizes of the sliders
        multiplicandSlider.setPrefWidth(400);
        pointsSlider.setPrefWidth(400);
        framerateSlider.setPrefWidth(400);
        incrementSlider.setPrefWidth(400);

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
        multiplicandSlider.setTranslateX(350);
        multiplicandSlider.setTranslateY(550);
        multiplicandLabel.setTranslateX(50);
        multiplicandLabel.setTranslateY(550);

        pointsSlider.setTranslateX(350);
        pointsSlider.setTranslateY(500);
        pointsLabel.setTranslateX(50);
        pointsLabel.setTranslateY(500);

        framerateSlider.setTranslateX(350);
        framerateSlider.setTranslateY(450);
        framerateLabel.setTranslateX(50);
        framerateLabel.setTranslateY(450);

        incrementSlider.setTranslateX(350);
        incrementSlider.setTranslateY(400);
        incrementLabel.setTranslateX(50);
        incrementLabel.setTranslateY(400);

        btn.setTranslateX(600);
        btn.setTranslateY(300);

        // Create a StackPane to overlay the Pane (canvas + slider)
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(pane);

        // Create a scene
        Scene scene = new Scene(stackPane, 800, 600);

        stage.setScene(scene);
        stage.show();

        animation.play();
    }

    private Button getButton() {
        Button btn = new Button();
        btn.setText ("Pause");
        btn.setPrefSize(100,50);

        // When the button is pressed, pause or play the animation
        btn.setOnAction(event -> {
            if (Objects.equals(btn.getText(), "Pause")) {
                animation.pause();
                btn.setText("Play");
                oldMultiplicand = multiplicand;
            } else {
                // If the multiplicand has changed since the button was pressed, restart the animation at that value
                if (oldMultiplicand != multiplicand) {
                    step = multiplicand;
                }
                animation.play();
                btn.setText("Pause");
            }
        });
        return btn;
    }

    private Slider getSlider(double min, double max, double start) {
        Slider slider = new Slider(min,max,start);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(40.0);

        slider.setPrefWidth(200);

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
        //int random = (int) (Math.random() * colors.size());
        Color choice = colors.get(colorIndex);//random);
        colorIndex++;
        if(colorIndex >= colors.size()) { colorIndex = 0; }
        gc.setStroke(choice);

        for(int m = 0; m < numPoints; m++) {
            int remainder = (int) ((multiplicand * m) % numPoints);
            gc.strokeLine(coordinates[m][0], coordinates[m][1], coordinates[remainder][0], coordinates[remainder][1]);
        }
    }

    private static List<Color> findColors() {
        List<Color> colors = new ArrayList<>();
        for(int hue = 0; hue < 360; hue += 5) {
            colors.add(Color.hsb(hue,1,1));
        }
        return colors;
    }

    // Found this on: https://stackoverflow.com/questions/17464906/how-to-list-all-colors-in-javafx
    private static List<Color> allColors() throws ClassNotFoundException, IllegalAccessException {
        List<Color> colors = new ArrayList<>();
        Class<?> clazz = Class.forName("javafx.scene.paint.Color");
        Field[] field = clazz.getFields();
        for (Field f : field) {
            Object obj = f.get(null);
            if (obj instanceof Color) {
                colors.add((Color) obj);
            }
        }
        return colors;
    }

    private void runAnimation(int[] offset, double diameter, double increment){
        step += increment;
        if(step > 360) { step = 0; }
        // Create a 2D array to store the coordinates of numPoints points around a circle
        double[][] coordinates = getPoints(numPoints, offset, diameter);

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawCircle(offset, diameter);
        drawLines(numPoints, step, coordinates);
    }

    private void updateSlider(Button btn, int[] offset, double diameter) {
        animation.pause();
        btn.setText("Play");
        // Create a 2D array to store the coordinates of numPoints points around a circle
        double[][] coordinates = getPoints(numPoints, offset, diameter);

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawCircle(offset, diameter);
        drawLines(numPoints, multiplicand, coordinates);
    }
}