package clock;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.util.StringJoiner;

/** Records the drawing commands from the unchanged, headless ClockCanvas. */
public final class ClockReference extends ClockCanvas {
    @Override public Image createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) {
            @Override public Graphics getGraphics() { return new Recorder(); }
        };
    }

    static final class Recorder extends Graphics {
        private Color color;
        private void record(String type, int... numbers) {
            StringJoiner row = new StringJoiner(",", "", "");
            row.add(type).add(Integer.toString(color.getRGB() & 0xFFFFFF));
            for (int value : numbers) row.add(Integer.toString(value));
            System.out.println(row);
        }
        @Override public void setColor(Color value) { color = value; }
        @Override public Color getColor() { return color; }
        @Override public void drawLine(int x, int y, int xx, int yy) { record("line", x,y,xx,yy); }
        @Override public void fillArc(int x, int y, int w, int h, int a, int b) { record("ring", x,y,w,h,a,b); }
        @Override public void fillPolygon(int[] x, int[] y, int length) {
            int[] vertices = new int[2*length];
            for (int i = 0; i < length; i++) {
                vertices[2*i] = x[i];
                vertices[2*i+1] = y[i];
            }
            record("triangle", vertices);
        }
        @Override public Graphics create() { throw unused(); }
        @Override public void translate(int x, int y) { throw unused(); }
        @Override public void setPaintMode() { throw unused(); }
        @Override public void setXORMode(Color c) { throw unused(); }
        @Override public Font getFont() { throw unused(); }
        @Override public void setFont(Font f) {}
        @Override public FontMetrics getFontMetrics(Font f) { throw unused(); }
        @Override public Rectangle getClipBounds() { throw unused(); }
        @Override public void clipRect(int x, int y, int w, int h) { throw unused(); }
        @Override public void setClip(int x, int y, int w, int h) { throw unused(); }
        @Override public Shape getClip() { throw unused(); }
        @Override public void setClip(Shape clip) { throw unused(); }
        @Override public void copyArea(int x, int y, int w, int h, int dx, int dy) { throw unused(); }
        @Override public void fillRect(int x, int y, int w, int h) { throw unused(); }
        @Override public void clearRect(int x, int y, int w, int h) { throw unused(); }
        @Override public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) { throw unused(); }
        @Override public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) { throw unused(); }
        @Override public void drawOval(int x, int y, int w, int h) { throw unused(); }
        @Override public void fillOval(int x, int y, int w, int h) { throw unused(); }
        @Override public void drawArc(int x, int y, int w, int h, int a, int b) { throw unused(); }
        @Override public void drawPolyline(int[] x, int[] y, int n) { throw unused(); }
        @Override public void drawPolygon(int[] x, int[] y, int n) { throw unused(); }
        @Override public void drawString(String s, int x, int y) { throw unused(); }
        @Override public void drawString(AttributedCharacterIterator s, int x, int y) { throw unused(); }
        @Override public boolean drawImage(Image i, int x, int y, ImageObserver o) { throw unused(); }
        @Override public boolean drawImage(Image i, int x, int y, int w, int h, ImageObserver o) { throw unused(); }
        @Override public boolean drawImage(Image i, int x, int y, Color c, ImageObserver o) { throw unused(); }
        @Override public boolean drawImage(Image i, int x, int y, int w, int h, Color c, ImageObserver o) { throw unused(); }
        @Override public boolean drawImage(Image i, int x, int y, int w, int h, int sx, int sy, int sw, int sh, ImageObserver o) { throw unused(); }
        @Override public boolean drawImage(Image i, int x, int y, int w, int h, int sx, int sy, int sw, int sh, Color c, ImageObserver o) { throw unused(); }
        @Override public void dispose() {}
        private UnsupportedOperationException unused() { return new UnsupportedOperationException(); }
    }

    public static void main(String[] args) {
        ClockReference clock = new ClockReference();
        clock.setSize(300, 300);
        clock.setClock(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        Graphics display = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB).getGraphics();
        clock.paint(display);
        display.dispose();
    }
}
