/*
 * The basis of DriSMo was developed as a bachelor project in 2011,
 * by three students at Gjøvik University College (Fredrik Kvitvik,
 * Fredrik Hørtvedt and Jørn André Myrland). For documentation on DriSMo,
 * view the JavaDoc provided with the source code.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.drismo.gui.monitor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import com.drismo.R;
import com.drismo.model.Quality;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Monitor that displays the realtime score, with a meter.
 */
public class QualityMeterMonitor extends MonitorActivityTemplate implements GLSurfaceView.Renderer {

    private static final String TAG = "QualityMeterMonitor";
    int score = 0;
    private float angle;

    private final QualityMeter qualityMonitor = new QualityMeter();
    private final PointArrow pointArrow  = new PointArrow();


    /**
     * Sets up the layout
     */
    @Override
    protected void setUpLayout() {
        GLSurfaceView mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(this);
        setContentView(mGLSurfaceView);
    }

    /**
     * Updates the score
     * @param newScore The new quality score.
     */
    @Override
    public void onQualityUpdate(int newScore) {
        score = newScore;
//        Log.d(TAG, "QualityUpdate!!");
    }

    /**
     * When the openGL surface is created.
     * @param gl the context
     * @param config the config
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        gl.glClearColor(0,0,0,1f);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_CULL_FACE);

        //load the texture
        qualityMonitor.loadGLTexture(gl, getApplicationContext());

    }

    /**
     * When the surface is changed, from portrait to landscape, set up the new projection.
     * @param gl the context
     * @param width the surface width
     * @param height the surface height
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {

         gl.glViewport(0, 0, width, height);

        float ratio= (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        if(width > height)
            gl.glOrthof(-ratio, ratio, -1, 1, 1, 10);
        else
            gl.glOrthof(-1, 1, -1 / ratio, 1 / ratio, 1, 10);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }


    /**
     * When a new is drawn
     * @param gl context
     */
    public void onDrawFrame(GL10 gl) {

        //clear the scene.
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping

                                                    //draw a wall, with the quality meter texture on.
        gl.glPushMatrix();
            gl.glTranslatef(0, 0, -2.5f);
            gl.glRotatef(-90, 0.0f, 0.0f, 1.0f);

            gl.glScalef(1.1f, 1.1f, 0);
            qualityMonitor.draw(gl);
        gl.glPopMatrix();
        gl.glDisable(GL10.GL_TEXTURE_2D);


        angle =0;                     //calculate where the arrow shall be.
        angle += (score - 1550);

        if(angle > 0)
            angle *= 1.3f;
        else
            angle *= 0.68f;

        if(angle > 155)
            angle = 155;
        else if(angle < -155)
            angle = -155;

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);   //blend the arrow with the qualitymeter tex.

        gl.glPushMatrix();
            gl.glTranslatef(0, 0, -1.1f);

            gl.glRotatef(-(angle), 0.0f, 0.0f, 1.0f);
            gl.glTranslatef(0, 0.62f, 0);

            gl.glRotatef(-180, 1.0f, 0.0f, 0.0f);
            gl.glScalef(0.3f, 0.3f, 0);
            pointArrow.draw(gl, Quality.getColorFromScore(score));

            gl.glScalef(1.2f, 1.1f, 0);
            pointArrow.draw(gl, Color.WHITE);                   //make a white border around the arrow.
        gl.glPopMatrix();
        gl.glDisable(GL10.GL_BLEND);


    }

    private class QualityMeter {

        /** The buffer holding the vertices */
        private FloatBuffer vertexBuffer;
        /** The buffer holding the texture coordinates */
        private FloatBuffer textureBuffer;
        /** The buffer holding the indices */
        private ByteBuffer indexBuffer;

        /** Our texture pointer */
        private int[] textures = new int[1];

        /**
         * The initial vertex definition
         */
        private float vertices[] = {
                            //Vertices according to faces
                            -1.0f, -1.0f, 1.0f, //Vertex 0
                            1.0f, -1.0f, 1.0f,  //v1
                            -1.0f, 1.0f, 1.0f,  //v2
                            1.0f, 1.0f, 1.0f   //v3
        };

        /** The initial texture coordinates (u, v) */
        private float texture[] = {
                            //Mapping coordinates for the vertices
                            0.0f, 0.0f,
                            0.0f, 1.0f,
                            1.0f, 0.0f,
                            1.0f, 1.0f
        };

        /** The initial indices definition */
        private byte indices[] = {
                            0,1,3, 0,3,2			//Face front
                                                };

        /**
         * The QualityMeter constructor.
         *
         * Initiate the buffers.
         */
        public QualityMeter() {
            //
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            vertexBuffer = byteBuf.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);

            //
            byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            textureBuffer = byteBuf.asFloatBuffer();
            textureBuffer.put(texture);
            textureBuffer.position(0);

            //
            indexBuffer = ByteBuffer.allocateDirect(indices.length);
            indexBuffer.put(indices);
            indexBuffer.position(0);
        }

        /**
         * The object own drawing function.
         * Called from the renderer to redraw this instance
         * with possible changes in values.
         *
         * @param gl - The GL Context
         */
        public void draw(GL10 gl) {
            //Bind our only previously generated texture in this case
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

            //Point to our buffers
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            //Set the face rotation
            gl.glFrontFace(GL10.GL_CCW);

            //Enable the vertex and texture state
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

            //Draw the vertices as triangles, based on the Index Buffer information
            gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);

            //Disable the client state before leaving
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        }

        /**
         * Load the textures
         *
         * @param gl - The GL Context
         * @param context - The Activity context
         */
        public void loadGLTexture(GL10 gl, Context context) {
            //Get the texture from the Android resource directory
            InputStream is = context.getResources().openRawResource(R.drawable.bg_monitor_quality);
            Bitmap bitmap = null;
            try {
                //BitmapFactory is an Android graphics utility for images
                bitmap = BitmapFactory.decodeStream(is);

            } finally {
                //Always clear and close
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                }
            }

            //Generate one texture pointer...
            gl.glGenTextures(1, textures, 0);
            //...and bind it to our array
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

            //Create Nearest Filtered Texture
                  /*
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

            //Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
                */

            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
            //if(gl instanceof GL11)
            gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

            //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
            //GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

            //Clean up
            bitmap.recycle();
        }
    }

    /**
     * This class is an object representation of
     * a PointArrow containing the vertex information,
     * color information and drawing functionality,
     * which is called by the renderer.
     */
    private class PointArrow {

        /** The buffer holding the vertices */
        private FloatBuffer vertexBuffer;
        /** The buffers holding the colors */
        private FloatBuffer blueBuffer;
        private FloatBuffer whiteBuffer;
        private FloatBuffer redBuffer;
        private FloatBuffer greenBuffer;
        private FloatBuffer yellowBuffer;

        /** The initial vertex definition */
        private float vertices[] = {
                                    0.0f, 1.0f, 0.0f, 	//Top
                                    -1.0f, -1.0f, 0.0f, //Bottom Left
                                    1.0f, -1.0f, 0.0f 	//Bottom Right
                                                    };

        /** The initial color definition */
        private float red[] = {   1.0f, 0.0f, 0.0f, 1.0f,
                                  0.0f, 0.0f, 0.0f, 0.0f,
                                  0.0f, 0.0f, 0.0f, 0.0f};

        private float white[] = { 1.0f, 1.0f, 1.0f, 1.0f,
                                  1.0f, 1.0f, 1.0f, 1.0f,
                                  1.0f, 1.0f, 1.0f, 1.0f};

        private float blue[] = {  0.0f, 0.0f, 1.0f, 1.0f,
                                  0.0f, 0.0f, 0.0f, 0.0f,
                                  0.0f, 0.0f, 0.0f, 0.0f};

        private float green[] = { 0.0f, 1.0f, 0.0f, 1.0f,
                                  0.0f, 0.0f, 0.0f, 0.0f,
                                  0.0f, 0.0f, 0.0f, 0.0f};

        private float yellow[] = { 1.0f, 1.0f, 0.0f, 1.0f,
                                   0.0f, 0.0f, 0.0f, 0.0f,
                                   0.0f, 0.0f, 0.0f, 0.0f};

        /**
         * The PointArrow constructor.
         *
         * Initiate the buffers.
         */
        public PointArrow() {
            //
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            vertexBuffer = byteBuf.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(red.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            redBuffer = byteBuf.asFloatBuffer();
            redBuffer.put(red);
            redBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(white.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            whiteBuffer = byteBuf.asFloatBuffer();
            whiteBuffer.put(white);
            whiteBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(blue.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            blueBuffer = byteBuf.asFloatBuffer();
            blueBuffer.put(blue);
            blueBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(green.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            greenBuffer = byteBuf.asFloatBuffer();
            greenBuffer.put(green);
            greenBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(yellow.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            yellowBuffer = byteBuf.asFloatBuffer();
            yellowBuffer.put(yellow);
            yellowBuffer.position(0);

        }

        /**
         * The object own drawing function.
         * Called from the renderer to redraw this instance
         * with possible changes in values.
         *
         * @param gl - The GL Context
         */
        public void draw(GL10 gl, final int color) {
            //Set the face rotation

            gl.glFrontFace(GL10.GL_CW);

            //Point to our buffers
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            //gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

            if(color ==Color.WHITE)
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, whiteBuffer);
            else if(color == Quality.UGLY_COLOR)
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, redBuffer);
            else if(color == Quality.GOOD_COLOR)
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, greenBuffer);
            else if(color == Quality.BAD_COLOR)
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, yellowBuffer);
            else
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, blueBuffer);

            //Enable the vertex and color state
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            //Draw the vertices as triangles
            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertices.length / 3);

            //Disable the client state before leaving
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        }
    }
}
