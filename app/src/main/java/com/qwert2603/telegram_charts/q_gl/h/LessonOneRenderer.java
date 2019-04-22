package com.qwert2603.telegram_charts.q_gl.h;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LessonOneRenderer implements GLSurfaceView.Renderer {
    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private float[] mProjectionMatrix = new float[16];

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private float[] mMVPMatrix = new float[16];

    /**
     * Store our model data in a float buffer.
     */
    private final FloatBuffer mTriangle1Vertices;
    private final FloatBuffer mTriangle2Vertices;
    private final FloatBuffer mTriangle3Vertices;
    private final FloatBuffer mTriangle4Vertices;
    private final FloatBuffer mTriangle5Vertices;
    private final FloatBuffer mTriangle6Vertices;


    /**
     * This will be used to pass in the transformation matrix.
     */
    private int mMVPMatrixHandle;

    /**
     * This will be used to pass in model position information.
     */
    private int mPositionHandle;

    /**
     * This will be used to pass in model color information.
     */
    private int mColorHandle;

    /**
     * How many bytes per float.
     */
    private final int mBytesPerFloat = 4;

    /**
     * How many elements per vertex.
     */
    private final int mStrideBytes = 7 * mBytesPerFloat;

    /**
     * Offset of the position data.
     */
    private final int mPositionOffset = 0;

    /**
     * Size of the position data in elements.
     */
    private final int mPositionDataSize = 3;

    /**
     * Offset of the color data.
     */
    private final int mColorOffset = 3;

    /**
     * Size of the color data in elements.
     */
    private final int mColorDataSize = 4;
    private float x;


    /**
     * Initialize the model data.
     */
    public LessonOneRenderer() {
        // Define points for equilateral triangles.

        // This triangle is white_blue.First sail is mainsail
        final float[] triangle1VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                0.0f, -0.25f, 0.0f,
                0.8f, 0.8f, 1.0f, 1.0f,

                0.0f, 0.56f, 0.0f,
                0.8f, 0.8f, 1.0f, 1.0f};
        // This triangle is white_blue..The second is called the jib sail
        final float[] triangle2VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.25f, -0.25f, 0.0f,
                0.8f, 0.8f, 1.0f, 1.0f,

                0.03f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                -0.25f, 0.4f, 0.0f,
                0.8f, 0.8f, 1.0f, 1.0f};

        // This triangle3 is blue.
        final float[] triangle3VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -1.0f, -1.5f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1.0f, -0.35f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                -1.0f, -0.35f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f};


        // This triangle4 is blue.
        final float[] triangle4VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -1.0f, -1.5f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1.0f, -1.5f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1.0f, -0.35f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f};

        // This triangle5 is brown.
        final float[] triangle5VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.4f, -0.3f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f,

                -0.4f, -0.4f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f,

                0.3f, -0.3f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f};

        // This triangle6 is brown.
        final float[] triangle6VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.4f, -0.4f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f,

                0.22f, -0.4f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f,

                0.3f, -0.3f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f};


        // Initialize the buffers.
        mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle2Vertices = ByteBuffer.allocateDirect(triangle2VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle3Vertices = ByteBuffer.allocateDirect(triangle3VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle4Vertices = ByteBuffer.allocateDirect(triangle4VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle5Vertices = ByteBuffer.allocateDirect(triangle5VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle6Vertices = ByteBuffer.allocateDirect(triangle6VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();


        mTriangle1Vertices.put(triangle1VerticesData).position(0);
        mTriangle2Vertices.put(triangle2VerticesData).position(0);
        mTriangle3Vertices.put(triangle3VerticesData).position(0);

        mTriangle4Vertices.put(triangle4VerticesData).position(0);
        mTriangle5Vertices.put(triangle5VerticesData).position(0);
        mTriangle6Vertices.put(triangle6VerticesData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.7f, 1.0f);

        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.5f, 0.0f, 0.0f, -5.0f, 0.0f, 1.0f, 0.0f);

        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"       // A constant representing the combined model/view/projection matrix.

                        + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"     // Per-vertex color information we will pass in.

                        + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.

                        + "void main()                    \n"     // The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"     // Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n"  // gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;       \n"       // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"     // This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"     // The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"     // Pass the color directly through the pipeline.
                        + "}                              \n";

        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        GLES20.glShaderSource(vertexShaderHandle, vertexShader);
        GLES20.glCompileShader(vertexShaderHandle);
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
        GLES20.glCompileShader(fragmentShaderHandle);
        int programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vertexShaderHandle);
        GLES20.glAttachShader(programHandle, fragmentShaderHandle);

        GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
        GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

        GLES20.glLinkProgram(programHandle);


        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        GLES20.glUseProgram(programHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, 0.0f, 0.0f);
        drawTriangle(mTriangle1Vertices);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x + 0.3f, 0.0f, 0.0f);
        drawTriangle(mTriangle2Vertices);

        if (x <= 1) {
            x = x + 0.001f;
        } else {
            x = 0;
        }

        Matrix.setIdentityM(mModelMatrix, 0);
        drawTriangle(mTriangle3Vertices);

        Matrix.setIdentityM(mModelMatrix, 0);
        drawTriangle(mTriangle4Vertices);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, 0.0f, 0.0f);
        drawTriangle(mTriangle5Vertices);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, 0.0f, 0.0f);
        drawTriangle(mTriangle6Vertices);

    }

    private void drawTriangle(final FloatBuffer aTriangleBuffer) {
        // Pass in the position information
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}