package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

/**
 * @author rushan
 * @since 04.05.2018
 */
public class GameScreen extends ScreenAdapter {
    private Drop game;
    private Camera camera;
    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private Rectangle bucket;
    private Vector3 touchPos = new Vector3();
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    int dropsGathered;

    public GameScreen(Drop game) {
        this.game = game;
        initCamera();
        dropImage = new Texture("droplet.png");
        bucketImage = new Texture("bucket.png");
        initBucket();
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        raindrops = new Array<Rectangle>();
        spawnRaindrop();
    }

    private void initBucket() {
        bucket = new Rectangle();
        bucket.height = 64;
        bucket.width = 64;
        bucket.y = 20;
        bucket.x = camera.viewportWidth / 2 - bucket.width / 2;
    }

    private void initCamera() {
        OrthographicCamera orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, 800, 480);
        camera = orthographicCamera;
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.height = 64;
        raindrop.width = 64;
        raindrop.y = camera.viewportHeight;
        raindrop.x = MathUtils.random(0, camera.viewportWidth - raindrop.width);
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "Drops collected: " + dropsGathered, 0, 480);
        batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        for (Rectangle raindrop : raindrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        batch.end();
        processUserInput();
        processRaindrops();
    }

    private void processRaindrops() {
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
            spawnRaindrop();
        }
        Iterator<Rectangle> raindropsIterator = raindrops.iterator();
        while (raindropsIterator.hasNext()) {
            Rectangle raindrop = raindropsIterator.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + raindrop.height < 0) {
                raindropsIterator.remove();
            }
            if (raindrop.overlaps(bucket)) {
                dropsGathered++;
                dropSound.play();
                raindropsIterator.remove();
            }
        }
    }

    private void processUserInput() {
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - bucket.width / 2;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucket.x += 200 * Gdx.graphics.getDeltaTime();
        }
        if (bucket.x < 0) {
            bucket.x = 0;
        }
        if (bucket.x > camera.viewportWidth - bucket.width) {
            bucket.x = camera.viewportWidth - bucket.width;
        }
    }

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        dropSound.dispose();
        bucketImage.dispose();
        rainMusic.dispose();
    }
}
