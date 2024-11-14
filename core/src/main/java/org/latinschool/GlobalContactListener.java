package org.latinschool;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.ContactImpulse;

public class GlobalContactListener implements ContactListener {

    private int isGroundedContacts = 0;

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (isFootSensor(fixtureA) || isFootSensor(fixtureB)) {
            isGroundedContacts += 1;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (isFootSensor(fixtureA) || isFootSensor(fixtureB)) {
            isGroundedContacts -= 1;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }

    private boolean isFootSensor(Fixture fixture) {
        return fixture.getUserData() != null && fixture.getUserData().equals("footSensor");
    }

    public boolean isGrounded() {
        return isGroundedContacts > 0;
    }
}
