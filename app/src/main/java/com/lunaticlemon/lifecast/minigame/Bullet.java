package com.lunaticlemon.lifecast.minigame;

import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.Random;

/**
 * Created by lemon on 2018-02-27.
 */

public class Bullet {
    private Point location;
    private Size size;
    private int min_x, max_x, min_y, max_y;
    private int x_velocity, y_velocity;
    private int space;

    public Bullet(int init_x, int init_y, int min_x, int max_x, int min_y, int max_y, Size size)
    {
        this.location = new Point();

        this.location.x = init_x;
        this.location.y = init_y;
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
        this.size = new Size(size.width, size.height);

        // bullet 이동속도 : 1~3 사이의 랜덤한 값을 가짐
        this.x_velocity = new Random().nextInt(10) + 1;
        this.y_velocity = new Random().nextInt(10) + 1;
        if(this.location.y != 0)
            this.y_velocity *= -1;

        this.space = 20;
    }

    public Point bullet_location()
    {
        return this.location;
    }

    // 움직일 수 있는 경우 이동하고 return true, 움직일 수 없으면 return false
    public boolean move(){
        location.x -= x_velocity;
        location.y += y_velocity;

        return !(location.x > max_x || location.x < min_x || location.y > max_y || location.y < min_y);
    }

    // 충돌 시 return true
    public boolean check_collision(Point astronaut, Size astronaut_size)
    {
        return Math.abs(location.x - astronaut.x) < ((astronaut_size.width + size.width - space) / 2) && Math.abs(location.y - astronaut.y) < ((astronaut_size.height + size.height - space) / 2);
    }
}
