package org.xtclang.examples;

import org.xtclang.jmixin.Mixin;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Examples of {@link org.xtclang.jmixin.Mixin}.
 *
 * @author falcom
 */
public class MixinExample
    {
    interface LongIdentityMixin extends Mixin
        {
        default long getId()
            {
            return mixin(State.class).id;
            }

        final class State extends Mixin.State
            {
            private static final AtomicLong ID_GENERATOR = new AtomicLong();
            private final long id = ID_GENERATOR.incrementAndGet();
            }
        }

    class Vehicle {
        String make;
        String model;
        int seating;
        public String getMake()
            {
            return make;
            }

        public void setMake(String make)
            {
            this.make = make;
            }

        public String getModel()
            {
            return model;
            }

        public void setModel(String model)
            {
            this.model = model;
            }
        public int getSeating()
            {
            return seating;
            }

        public void setSeating(int seating)
            {
            this.seating = seating;
            }
        }

    class Car extends Vehicle
        {
        boolean frontWheelDrive;
        boolean manualTransmission;
        public boolean isFrontWheelDrive()
            {
            return frontWheelDrive;
            }

        public void setFrontWheelDrive(boolean frontWheelDrive)
            {
            this.frontWheelDrive = frontWheelDrive;
            }
        public boolean isManualTransmission()
            {
            return manualTransmission;
            }

        public void setManualTransmission(boolean manualTransmission)
            {
            this.manualTransmission = manualTransmission;
            }
        }

    abstract class Boat extends Vehicle
        {
        int displacement;
        public int getDisplacement()
            {
            return displacement;
            }

        public void setDisplacement(int displacement)
            {
            this.displacement = displacement;
            }
        }

    interface VehicleMixin extends Mixin
        {
        default String getMake()
            {
            return mixin(State.class).make;
            }
    
        default void setMake(String make)
            {
            mixin(State.class).make = make;
            }
    
        default String getModel()
            {
            return mixin(State.class).model;
            }
    
        default void setModel(String model)
            {
            mixin(State.class).model = model;
            }
        default int getSeating()
            {
            return mixin(State.class).seating;
            }
    
        default void setSeating(int seating)
            {
            mixin(State.class).seating = seating;
            }
        
        final class State extends Mixin.State
            {
            String make;
            String model;
            int seating;
            }
        }

    interface CarMixin extends VehicleMixin
        {
        default boolean isFrontWheelDrive()
            {
            return mixin(State.class).frontWheelDrive;
            }

        default void setFrontWheelDrive(boolean frontWheelDrive)
            {
            mixin(State.class).frontWheelDrive = frontWheelDrive;
            }
        default boolean isManualTransmission()
            {
            return mixin(State.class).manualTransmission;
            }

        default void setManualTransmission(boolean manualTransmission)
            {
            mixin(State.class).manualTransmission = manualTransmission;
            }

        final class State extends Mixin.State
            {
            boolean frontWheelDrive;
            boolean manualTransmission;
            }
        }

    interface BoatMixin extends VehicleMixin
        {
        default int getDisplacement()
            {
            return mixin(State.class).displacement;
            }

        default void setDisplacement(int displacement)
            {
            mixin(State.class).displacement = displacement;
            }

        final class State extends Mixin.State
            {
            int displacement;
            }
        }

    class Beetle extends Mixin.Base implements CarMixin
        {
        public Beetle()
            {
            setMake("VW");
            setModel("Beetle");
            setSeating(4);
            setFrontWheelDrive(true);
            setManualTransmission(true);
            }
        }

    class Schwimmwagen extends Mixin.Base implements CarMixin, BoatMixin
        {
        public Schwimmwagen()
            {
            setMake("VW");
            setModel("Schwimmwagen");
            setSeating(2);
            setFrontWheelDrive(false);
            setManualTransmission(true);
            setDisplacement(1234);
            }
        }
    }
