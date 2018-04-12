package Vista.ToDraw;

import Vista.GraphicsPanel;

import java.awt.*;

public interface ToDraw {
    void init(GraphicsPanel graphicsPanel);
    void update(float delta);
    void render(Graphics g);
    void updateSize(int width, int height);
}