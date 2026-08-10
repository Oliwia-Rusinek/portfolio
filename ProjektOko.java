import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class Main extends JFrame implements GLEventListener, KeyListener {

    private static final long serialVersionUID = 1L;

    private GL2 gl;
    private GLU glu;
    private GLUT glut;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean blinkPressed = false;

    private float eyeOffsetX = 0.0f;
    private float eyeOffsetY = 0.0f;

    private float eyelidClose = 0.0f;
    private boolean blinking = false;
    private boolean closing = true;
    private long lastBlinkTime = System.currentTimeMillis();

    private float headRotateY = 0.0f;
    private float targetHeadRotateY = 0.0f;

    private int skinType = 0;
    private int eyeColorType = 0;
    private int hairType = 0;
    private int backgroundType = 0;
    private int expressionType = 0;

    // kamera
    private float camAngleY = 0f;
    private float camAngleX = 12f;
    private float camDist = 11.5f;

    private final float[][] skinColors = {
        {0.98f, 0.82f, 0.70f},
        {0.82f, 0.66f, 0.52f},
        {0.60f, 0.45f, 0.32f}
    };

    private final float[][] eyeColors = {
        {0.10f, 0.68f, 0.96f},
        {0.20f, 0.75f, 0.30f},
        {0.55f, 0.35f, 0.85f}
    };

    private final float[][] bgColors = {
        {0.82f, 0.89f, 0.98f},
        {0.98f, 0.92f, 0.94f},
        {0.84f, 0.84f, 0.94f}
    };

    public Main(String title) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(d.width / 4, d.height / 4, d.width / 2, d.height / 2);

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        canvas.setFocusable(true);

        add(canvas);
        setVisible(true);

        FPSAnimator animator = new FPSAnimator(canvas, 60, true);
        animator.start();
        canvas.requestFocusInWindow();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();

        gl.glClearColor(0.90f, 0.92f, 0.98f, 1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        float[] light0Pos = { 5.0f, 6.0f, 9.0f, 1.0f };
        float[] light0Amb = { 0.35f, 0.35f, 0.35f, 1.0f };
        float[] light0Dif = { 0.98f, 0.98f, 0.98f, 1.0f };

        float[] light1Pos = { -4.0f, 2.0f, 6.0f, 1.0f };
        float[] light1Dif = { 0.22f, 0.22f, 0.30f, 1.0f };

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0Pos, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light0Amb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light0Dif, 0);

        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, light1Pos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, light1Dif, 0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        if (height <= 0) height = 1;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, (double) width / height, 1.0, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        updateAnimation();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        float camX = (float) (Math.sin(Math.toRadians(camAngleY)) * camDist);
        float camZ = (float) (Math.cos(Math.toRadians(camAngleY)) * camDist);
        float camY = (float) (Math.sin(Math.toRadians(camAngleX)) * camDist * 0.55f);

        glu.gluLookAt(camX, camY, camZ,
                      0.0, 0.0, 0.0,
                      0.0, 1.0, 0.0);

        drawBackground();
        drawFace();
        drawInstructionBar();
    }

    private void updateAnimation() {
        float speed = 0.03f;
        float max = 0.18f;

        if (leftPressed) eyeOffsetX -= speed;
        if (rightPressed) eyeOffsetX += speed;
        if (upPressed) eyeOffsetY += speed;
        if (downPressed) eyeOffsetY -= speed;

        if (eyeOffsetX > max) eyeOffsetX = max;
        if (eyeOffsetX < -max) eyeOffsetX = -max;
        if (eyeOffsetY > max) eyeOffsetY = max;
        if (eyeOffsetY < -max) eyeOffsetY = -max;

        targetHeadRotateY = eyeOffsetX * 20f;
        headRotateY += (targetHeadRotateY - headRotateY) * 0.08f;

        long now = System.currentTimeMillis();
        if (!blinking && now - lastBlinkTime > 2800) {
            blinking = true;
            closing = true;
        }

        if (blinkPressed && !blinking) {
            blinking = true;
            closing = true;
            blinkPressed = false;
        }

        if (blinking) {
            if (closing) {
                eyelidClose += 0.12f;
                if (eyelidClose >= 1f) {
                    eyelidClose = 1f;
                    closing = false;
                }
            } else {
                eyelidClose -= 0.12f;
                if (eyelidClose <= 0f) {
                    eyelidClose = 0f;
                    blinking = false;
                    closing = true;
                    lastBlinkTime = now;
                }
            }
        }
    }

    private void drawBackground() {
        float[] c = bgColors[backgroundType];

        gl.glDisable(GL2.GL_LIGHTING);
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(c[0], c[1], c[2]);
        gl.glVertex3f(-20f, 12f, -15f);
        gl.glVertex3f(20f, 12f, -15f);
        gl.glColor3f(Math.min(c[0] + 0.08f, 1.0f), Math.min(c[1] + 0.08f, 1.0f), Math.min(c[2] + 0.08f, 1.0f));
        gl.glVertex3f(20f, -12f, -15f);
        gl.glVertex3f(-20f, -12f, -15f);
        gl.glEnd();
        gl.glEnable(GL2.GL_LIGHTING);
    }

    private void drawFace() {
        gl.glPushMatrix();
        gl.glRotatef(headRotateY, 0f, 1f, 0f);

        drawEar(-2.10f, 0.05f, 0.55f);
        drawEar(2.10f, 0.05f, 0.55f);
        drawHairCap();

        gl.glPushMatrix();
        gl.glScalef(2.5f, 3.1f, 1.9f);
        float[] skin = skinColors[skinType];
        gl.glColor3f(skin[0], skin[1], skin[2]);
        glut.glutSolidSphere(1.0, 48, 48);
        gl.glPopMatrix();

        drawFringe();
        drawCheek(-1.15f, -0.15f, 1.55f);
        drawCheek(1.15f, -0.15f, 1.55f);

        drawEyebrow(-1.0f, 1.18f, 1.80f, -14f);
        drawEyebrow(1.0f, 1.18f, 1.80f, 14f);

        drawEye(-0.95f, 0.55f, 1.82f);
        drawEye(0.95f, 0.55f, 1.82f);

        drawNose();
        drawMouth();

        gl.glPopMatrix();
    }

    private void drawEar(float x, float y, float z) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        gl.glScalef(0.32f, 0.62f, 0.20f);
        float[] skin = skinColors[skinType];
        gl.glColor3f(skin[0], skin[1], skin[2]);
        glut.glutSolidSphere(1.0, 24, 24);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(x * 0.98f, y, z + 0.10f);
        gl.glScalef(0.14f, 0.28f, 0.08f);
        gl.glColor3f(0.92f, 0.68f, 0.68f);
        glut.glutSolidSphere(1.0, 16, 16);
        gl.glPopMatrix();
    }

    private void drawHairCap() {
        gl.glPushMatrix();

        if (hairType == 0) gl.glColor3f(0.22f, 0.12f, 0.05f);
        else if (hairType == 1) gl.glColor3f(0.90f, 0.80f, 0.30f);
        else gl.glColor3f(0.08f, 0.08f, 0.08f);

        gl.glTranslatef(0.0f, 1.95f, -0.05f);
        gl.glScalef(2.25f, 1.20f, 1.75f);
        glut.glutSolidSphere(1.0, 40, 40);
        gl.glPopMatrix();

        gl.glPushMatrix();
        if (hairType == 0) gl.glColor3f(0.18f, 0.10f, 0.04f);
        else if (hairType == 1) gl.glColor3f(0.82f, 0.72f, 0.22f);
        else gl.glColor3f(0.05f, 0.05f, 0.05f);
        gl.glTranslatef(0.0f, 1.55f, -0.55f);
        gl.glScalef(2.10f, 1.75f, 1.45f);
        glut.glutSolidSphere(1.0, 36, 36);
        gl.glPopMatrix();

        if (hairType == 0) {
            drawSideHair(2.00f, 0.70f, 0.25f, 0.32f, 1.60f, 0.78f);
            drawSideHair(-2.00f, 0.70f, 0.25f, 0.32f, 1.60f, 0.78f);
        } else if (hairType == 1) {
            drawSideHair(2.00f, 0.15f, 0.20f, 0.36f, 2.25f, 0.82f);
            drawSideHair(-2.00f, 0.15f, 0.20f, 0.36f, 2.25f, 0.82f);
        } else {
            drawSideHair(2.00f, 1.00f, 0.22f, 0.28f, 1.00f, 0.72f);
            drawSideHair(-2.00f, 1.00f, 0.22f, 0.28f, 1.00f, 0.72f);
        }
    }

    private void drawSideHair(float x, float y, float z, float sx, float sy, float sz) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        gl.glScalef(sx, sy, sz);
        glut.glutSolidSphere(1.0, 28, 28);
        gl.glPopMatrix();
    }

    private void drawFringe() {
        gl.glPushMatrix();

        if (hairType == 0) gl.glColor3f(0.24f, 0.13f, 0.06f);
        else if (hairType == 1) gl.glColor3f(0.95f, 0.82f, 0.35f);
        else gl.glColor3f(0.10f, 0.10f, 0.10f);

        if (hairType == 0) {
            gl.glTranslatef(0.0f, 1.72f, 1.20f);
            gl.glScalef(1.85f, 0.55f, 0.34f);
        } else if (hairType == 1) {
            gl.glTranslatef(0.0f, 1.82f, 1.15f);
            gl.glScalef(1.70f, 0.40f, 0.30f);
        } else {
            gl.glTranslatef(0.0f, 1.62f, 1.22f);
            gl.glScalef(1.95f, 0.65f, 0.35f);
        }

        glut.glutSolidSphere(1.0, 28, 28);
        gl.glPopMatrix();
    }

    private void drawCheek(float x, float y, float z) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        gl.glScalef(0.35f, 0.20f, 0.08f);
        gl.glColor3f(0.95f, 0.60f, 0.65f);
        glut.glutSolidSphere(1.0, 20, 20);
        gl.glPopMatrix();
    }

    private void drawEyebrow(float x, float y, float z, float rotZ) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        gl.glRotatef(rotZ, 0f, 0f, 1f);
        gl.glScalef(0.60f, 0.08f, 0.10f);
        gl.glColor3f(0.35f, 0.18f, 0.08f);
        glut.glutSolidCube(1.0f);
        gl.glPopMatrix();
    }

    private void drawEye(float x, float y, float z) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);

        float eyeScaleY = 0.36f;
        if (expressionType == 1) eyeScaleY = 0.30f;
        if (expressionType == 2) eyeScaleY = 0.26f;
        if (expressionType == 4) eyeScaleY = 0.34f;

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -0.02f, -0.02f);
        gl.glScalef(0.82f, 0.40f, 0.16f);
        gl.glColor3f(0.88f, 0.78f, 0.72f);
        glut.glutSolidSphere(1.0, 24, 24);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(0.78f, eyeScaleY, 0.22f);
        gl.glColor3f(1f, 1f, 1f);
        glut.glutSolidSphere(1.0, 30, 30);
        gl.glPopMatrix();

        float[] eyeC = eyeColors[eyeColorType];

        gl.glPushMatrix();
        gl.glTranslatef(eyeOffsetX * 0.55f, eyeOffsetY * 0.45f, 0.16f);
        gl.glScalef(0.20f, 0.20f, 0.06f);
        gl.glColor3f(eyeC[0], eyeC[1], eyeC[2]);
        glut.glutSolidSphere(1.0, 22, 22);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(eyeOffsetX * 0.62f, eyeOffsetY * 0.50f, 0.23f);
        gl.glScalef(0.08f, 0.08f, 0.03f);
        gl.glColor3f(0.05f, 0.05f, 0.05f);
        glut.glutSolidSphere(1.0, 18, 18);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(eyeOffsetX * 0.62f - 0.04f, eyeOffsetY * 0.50f + 0.05f, 0.26f);
        gl.glScalef(0.028f, 0.028f, 0.02f);
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        glut.glutSolidSphere(1.0, 10, 10);
        gl.glPopMatrix();

        drawEyelashes();

        float[] skin = skinColors[skinType];

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, 0.22f - eyelidClose * 0.22f, 0.12f);
        gl.glScalef(0.78f, 0.18f, 0.12f);
        gl.glColor3f(skin[0], skin[1], skin[2]);
        glut.glutSolidSphere(1.0, 24, 24);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -0.24f + eyelidClose * 0.10f, 0.10f);
        gl.glScalef(0.72f, 0.10f, 0.10f);
        gl.glColor3f(skin[0], skin[1], skin[2]);
        glut.glutSolidSphere(1.0, 20, 20);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }

    private void drawEyelashes() {
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(0.18f, 0.10f, 0.08f);
        gl.glLineWidth(2f);
        gl.glBegin(GL2.GL_LINES);

        gl.glVertex3f(-0.32f, 0.22f, 0.18f);
        gl.glVertex3f(-0.42f, 0.34f, 0.18f);

        gl.glVertex3f(-0.16f, 0.26f, 0.18f);
        gl.glVertex3f(-0.20f, 0.40f, 0.18f);

        gl.glVertex3f(0.0f, 0.28f, 0.18f);
        gl.glVertex3f(0.0f, 0.43f, 0.18f);

        gl.glVertex3f(0.16f, 0.26f, 0.18f);
        gl.glVertex3f(0.20f, 0.40f, 0.18f);

        gl.glVertex3f(0.32f, 0.22f, 0.18f);
        gl.glVertex3f(0.42f, 0.34f, 0.18f);

        gl.glEnd();
        gl.glEnable(GL2.GL_LIGHTING);
    }

    private void drawNose() {
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, 0.05f, 1.75f);
        gl.glRotatef(-90f, 1f, 0f, 0f);
        gl.glColor3f(0.95f, 0.70f, 0.55f);
        glut.glutSolidCone(0.22, 0.8, 20, 20);
        gl.glPopMatrix();
    }

    private void drawMouth() {
        gl.glDisable(GL2.GL_LIGHTING);

        if (expressionType == 3) {
            gl.glColor3f(0.45f, 0.02f, 0.08f);
            gl.glLineWidth(3f);
            gl.glBegin(GL2.GL_LINE_LOOP);
            for (int i = 0; i < 40; i++) {
                float a = (float) (2.0 * Math.PI * i / 40.0);
                float x = (float) (Math.cos(a) * 0.42f);
                float y = (float) (Math.sin(a) * 0.26f);
                gl.glVertex3f(x, y - 1.22f, 1.84f);
            }
            gl.glEnd();

            gl.glColor3f(0.30f, 0.00f, 0.03f);
            gl.glBegin(GL2.GL_POLYGON);
            for (int i = 0; i < 40; i++) {
                float a = (float) (2.0 * Math.PI * i / 40.0);
                float x = (float) (Math.cos(a) * 0.38f);
                float y = (float) (Math.sin(a) * 0.22f);
                gl.glVertex3f(x, y - 1.22f, 1.83f);
            }
            gl.glEnd();

            gl.glColor3f(0.95f, 0.45f, 0.60f);
            gl.glBegin(GL2.GL_POLYGON);
            for (int i = 0; i < 32; i++) {
                float a = (float) (2.0 * Math.PI * i / 32.0);
                float x = (float) (Math.cos(a) * 0.18f);
                float y = (float) (Math.sin(a) * 0.10f);
                gl.glVertex3f(x, y - 1.30f, 1.845f);
            }
            gl.glEnd();

            gl.glEnable(GL2.GL_LIGHTING);
            return;
        }

        if (expressionType == 4) {
            gl.glColor3f(0.72f, 0.05f, 0.18f);
            gl.glLineWidth(4f);
            gl.glBegin(GL2.GL_LINE_LOOP);
            for (int i = 0; i < 40; i++) {
                float a = (float) (2.0 * Math.PI * i / 40.0);
                float x = (float) (Math.cos(a) * 0.58f);
                float y = (float) (Math.sin(a) * 0.30f);
                gl.glVertex3f(x, y - 1.18f, 1.84f);
            }
            gl.glEnd();

            gl.glColor3f(0.25f, 0.00f, 0.02f);
            gl.glBegin(GL2.GL_POLYGON);
            for (int i = 0; i < 40; i++) {
                float a = (float) (2.0 * Math.PI * i / 40.0);
                float x = (float) (Math.cos(a) * 0.52f);
                float y = (float) (Math.sin(a) * 0.25f);
                gl.glVertex3f(x, y - 1.18f, 1.83f);
            }
            gl.glEnd();

            gl.glColor3f(1.0f, 1.0f, 1.0f);
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex3f(-0.44f, -1.02f, 1.845f);
            gl.glVertex3f(0.44f, -1.02f, 1.845f);
            gl.glVertex3f(0.44f, -1.16f, 1.845f);
            gl.glVertex3f(-0.44f, -1.16f, 1.845f);
            gl.glEnd();

            gl.glColor3f(0.78f, 0.78f, 0.78f);
            gl.glLineWidth(1f);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(-0.22f, -1.02f, 1.846f); gl.glVertex3f(-0.22f, -1.16f, 1.846f);
            gl.glVertex3f(0.0f, -1.02f, 1.846f);   gl.glVertex3f(0.0f, -1.16f, 1.846f);
            gl.glVertex3f(0.22f, -1.02f, 1.846f);  gl.glVertex3f(0.22f, -1.16f, 1.846f);
            gl.glEnd();

            gl.glEnable(GL2.GL_LIGHTING);
            return;
        }

        gl.glColor3f(0.82f, 0.05f, 0.24f);
        gl.glLineWidth(4f);

        gl.glBegin(GL2.GL_LINE_STRIP);
        for (int i = 0; i <= 30; i++) {
            float t = (float) i / 30f;
            float x = -0.78f + 1.56f * t;
            float y;

            if (expressionType == 0) y = -1.16f + 0.06f * (float) Math.sin(t * Math.PI);
            else if (expressionType == 1) y = -1.14f + 0.14f * (float) Math.sin(t * Math.PI);
            else y = -1.19f + 0.02f * (float) Math.sin(t * Math.PI);

            gl.glVertex3f(x, y, 1.84f);
        }
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_STRIP);
        for (int i = 0; i <= 30; i++) {
            float t = (float) i / 30f;
            float x = -0.78f + 1.56f * t;
            float y;

            if (expressionType == 0) y = -1.30f - 0.04f * (float) Math.sin(t * Math.PI);
            else if (expressionType == 1) y = -1.24f - 0.12f * (float) Math.sin(t * Math.PI);
            else y = -1.28f - 0.16f * (float) Math.sin(t * Math.PI);

            gl.glVertex3f(x, y, 1.84f);
        }
        gl.glEnd();

        gl.glColor3f(0.50f, 0.01f, 0.10f);
        gl.glLineWidth(2f);
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (int i = 0; i <= 30; i++) {
            float t = (float) i / 30f;
            float x = -0.66f + 1.32f * t;
            float y;

            if (expressionType == 0) y = -1.22f;
            else if (expressionType == 1) y = -1.19f + 0.04f * (float) Math.sin(t * Math.PI);
            else y = -1.23f - 0.05f * (float) Math.sin(t * Math.PI);

            gl.glVertex3f(x, y, 1.85f);
        }
        gl.glEnd();

        gl.glEnable(GL2.GL_LIGHTING);
    }

    private void drawInstructionBar() {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(-1, 1, -1, 1, -1, 1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(0.14f, 0.16f, 0.22f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(-1.0f, -1.0f);
        gl.glVertex2f(1.0f, -1.0f);
        gl.glVertex2f(1.0f, -0.82f);
        gl.glVertex2f(-1.0f, -0.82f);
        gl.glEnd();

        gl.glColor3f(1f, 1f, 1f);
        gl.glRasterPos2f(-0.97f, -0.93f);
        String text = "A/D obrót  W/S góra-dół  Q/E zoom  1 skin 2 eyes 4 hair 5 bg 6 exp";
        for (int i = 0; i < text.length(); i++) {
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, text.charAt(i));
        }

        gl.glEnable(GL2.GL_LIGHTING);

        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_UP:
                upPressed = true;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = true;
                break;
            case KeyEvent.VK_SPACE:
                blinkPressed = true;
                break;

            case KeyEvent.VK_A:
                camAngleY -= 5f;
                break;
            case KeyEvent.VK_D:
                camAngleY += 5f;
                break;
            case KeyEvent.VK_W:
                camAngleX += 3f;
                if (camAngleX > 80f) camAngleX = 80f;
                break;
            case KeyEvent.VK_S:
                camAngleX -= 3f;
                if (camAngleX < -45f) camAngleX = -45f;
                break;
            case KeyEvent.VK_Q:
                camDist -= 0.5f;
                if (camDist < 5f) camDist = 5f;
                break;
            case KeyEvent.VK_E:
                camDist += 0.5f;
                if (camDist > 20f) camDist = 20f;
                break;

            case KeyEvent.VK_1:
                skinType = (skinType + 1) % 3;
                break;
            case KeyEvent.VK_2:
                eyeColorType = (eyeColorType + 1) % 3;
                break;
            case KeyEvent.VK_4:
                hairType = (hairType + 1) % 3;
                break;
            case KeyEvent.VK_5:
                backgroundType = (backgroundType + 1) % 3;
                break;
            case KeyEvent.VK_6:
                expressionType = (expressionType + 1) % 5;
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_UP:
                upPressed = false;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main("Kreator postaci 3D");
            }
        });
    }
}