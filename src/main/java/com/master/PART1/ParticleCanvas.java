package com.master.PART1;

import java.awt.*;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-01-23 12:57
 */
public class ParticleCanvas extends Canvas {
    private Particle[] particles=new Particle[0];
    ParticleCanvas(int size){
        setSize(new Dimension(size,size
        ));
    }

    synchronized Particle[] getParticles() {
        return particles;
    }

    synchronized void setParticles(Particle[] particles) {
        if(particles==null){
            throw new IllegalArgumentException("can not set null");
        }
        this.particles = particles;
    }

    public void print(Graphics g){
        Particle[] particles1 = getParticles();
        for (Particle p:particles1
             ) {
            p.draw(g);
        }
    }
}
