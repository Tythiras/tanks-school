package com.school.tanksgame.sprites;

import com.school.tanksgame.Constants;
import com.school.tanksgame.Controls;
import processing.core.PConstants;
import processing.core.PVector;
import processing.event.KeyEvent;

import java.util.ArrayList;
import java.util.Map;

public class Tank extends Sprite {
    Map<Integer, Controls> controls;
    ArrayList<Bullet> bullets;

    private PVector location;
    private float rotation;

    private int color;
    private float health = Constants.TANK_START_HEALTH;

    private boolean shoot;

    private int fireDelay = 0;

    private boolean rotatingUp;
    private boolean rotatingDown;
    private boolean drivingForwards;
    private boolean drivingBackwards;

    public Tank(PVector location, Map<Integer, Controls> controls, int color) {
        this.controls = controls;
        this.location = location;

        this.rotation = 0;

        this.color = color;

        this.bullets = new ArrayList<>();

        this.rotatingUp = false;
        this.rotatingDown = false;
        this.drivingForwards = false;
        this.drivingBackwards = false;
        this.shoot = false;
    }

    public PVector getLocation() {
        return location;
    }

    public void shoot() {
        PVector dirVec = PVector.fromAngle(this.rotation);
        PVector startLoc = this.location.copy().add(dirVec.setMag((Constants.TANK_HEIGHT / 2) +Constants.TANK_SHAFT_HEIGHT));

        Bullet bullet = new Bullet(startLoc, this.rotation);
        bullet.setParent(parent);
        bullets.add(bullet);
    }

    public void detectCollision(Wall wall) {
        for(Bullet bullet : bullets) {
            float dist = wall.distanceToPoint(bullet.location);
            System.out.println(wall.getA()+" : "+dist);
            if(dist<Constants.WALL_WIDTH / 2 + Constants.BULLET_RADIUS) {
                PVector normal = new PVector(1, -1 / wall.getA()).normalize();
                PVector curr = bullet.getVelocity().copy();
                float dotProduct = normal.dot(curr);

                bullet.setVelocity(normal.mult(2*dotProduct).sub(curr));
                System.out.println("Reflect");
            }
        }
    }

    public void keyAction(KeyEvent event) {
       Controls control = controls.get(event.getKeyCode());
       int keyAction = event.getAction();

       if (control == null)
           return;

       switch (control) {
           // keyAction == 1 means that it's keyPressed, otherwise keyReleased
           case SHOOT:
               shoot = keyAction == 1;
               break;
           case ROTATE_UP:
               rotatingUp = keyAction == 1;
               break;
           case ROTATE_DOWN:
               rotatingDown = keyAction == 1;
               break;
           case DRIVING_FORWARD:
               drivingForwards = keyAction == 1;
               break;
           case DRIVING_BACKWARDS:
               drivingBackwards = keyAction == 1;
               break;
       }
    }

    public void update() {
        //update rotation
        if (rotatingUp) {
            rotation += Constants.TANK_ROTATIONAL_VEL;
            if (rotation > 2*Math.PI) {
                rotation = 0;
            }
        }
        else if (rotatingDown) {
            rotation -= Constants.TANK_ROTATIONAL_VEL;
            if (rotation < 0) {
                rotation = (float) (2*Math.PI);
            }
        }

        //update location and make sure it isn't over the window boundaries
        PVector newLoc = new PVector(location.x, location.y);
        PVector dirVec = PVector.fromAngle(rotation);
        dirVec.setMag(Constants.TANK_DRIVING_VEL);

        if (drivingForwards) {
            newLoc.add(dirVec);
        }
        if (drivingBackwards) {
            dirVec.mult(-1);
            newLoc.add(dirVec);
        }
        if(newLoc.x < parent.width && newLoc.x > 0 && newLoc.y < parent.height && newLoc.y > 0 ) {
            location = newLoc;
        }

        //shoot if button is pressed
        if(shoot) {
            this.shoot();
            //no machine guns here
            shoot = false;
        }


        //update bullets
        for(int i = bullets.size(); i > 0; i--) {
            Bullet bullet = bullets.get(i-1);
            bullet.update();
            if(!bullet.isAlive()) {
                bullets.remove(bullet);
            }
        }
    }

    public void draw() {
        if(health>0) {
            parent.stroke(0);
            parent.strokeWeight(1);
            parent.pushMatrix();
            parent.translate(location.x, location.y);

            parent.rectMode(PConstants.CORNER);
            //health bar
            float healthFactor = (255 / Constants.TANK_START_HEALTH) * this.health;
            parent.fill(255);
            parent.rect(-Constants.HEALTHBAR_WIDTH / 2, -(Constants.HEALTHBAR_OFFSET + Constants.HEALTHBAR_HEIGHT), Constants.HEALTHBAR_WIDTH, Constants.HEALTHBAR_HEIGHT);
            parent.fill(255 - healthFactor, healthFactor, 0);
            parent.rect(-Constants.HEALTHBAR_WIDTH / 2, -(Constants.HEALTHBAR_OFFSET + Constants.HEALTHBAR_HEIGHT), (Constants.HEALTHBAR_WIDTH / Constants.TANK_START_HEALTH) * this.health, Constants.HEALTHBAR_HEIGHT);


            parent.rectMode(PConstants.CENTER);
            //main body and shaft
            parent.fill(parent.color(this.color));
            parent.rotate(rotation);
            //main body
            parent.rect(0, 0, Constants.TANK_WIDTH, Constants.TANK_HEIGHT);

            //cannon shaft
            float shaftWidth = Constants.TANK_SHAFT_WIDTH;
            float shaftHeight = Constants.TANK_SHAFT_HEIGHT;
            parent.rect(shaftHeight / 2 + Constants.TANK_WIDTH / 2, 0, shaftHeight, shaftWidth);

            parent.popMatrix();

            //draw bullets
            for (Bullet bullet : bullets) {
                bullet.draw();
            }
        }
    }
}
