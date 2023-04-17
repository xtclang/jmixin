# JMixin

## Introduction

One of the problems with working in Ecstasy is that it becomes really annoying to move back to "lesser" languages ;). One Ecstasy feature I miss when working in Java is mixins. You don't need them often, but when you do mixins can be very very useful.

In response this JMixin project was created to bring Ecstasy style mixins to Java. While not quite as capable as full-blown Ecstasy mixins, JMixins do get close enough to be quite useful.

## Mixins

But what are mixins in the first place? Simply put mixins offer what is essentially multiple inheritance while avoiding the classic [diamond problem](https://en.wikipedia.org/wiki/Multiple_inheritance#The_diamond_problem). Like many object-oriented languages, Java only supports a single line of inheritance. Inheritance is very useful in that it allows you to "inherit" implementation logic in a new class without having to actually provide (or repeat) that logic. For
example lets imagine we're going to have classes describing different types of vehicles for (land, sea, and air). We could create classes `Bicycle`, `Car`, `Truck`, `Boat`, `Sub`, `Plane`, `Helicopter`, etc. All of these classes have at least a loose relationship, they are all types of vehicles. So we can save ourselves some work and define a base `Vehicle` class which specifics the properties which are common across all of these:

```
class Vehicle 
    {
    public int getSeats()
        {
        return this.seats;
        }
        
    public void setSeats(int seats)
        {
        this.seats = seats;
        }
        
    public int getStorageCapacityFt3()
        {
        return this.storageCapacityFt3;
        }
        
    public void setStorageCapacityFt3(int storageCapacityFt3)
        {
        this.storageCapacityFt3 = storageCapacityFt3;
        }
        
    private int seats;
    private int storageCapacityFt3;  
    }
```

Now we could have all of the above classes extend `Vehicle` and we get to avoid redefining the `seats` and `storageCapacity` fields and accessors over and over again. We can also use multiple levels of inheritance to do some more sharing. Perhaps we should have `LandVehicle` which extends `Vehicle` and which is extended by `Car` and `Truck` and defines something like how many doors, and wheels they have. And similarly we could introduce `SeaVehicle` abstracting out the commonalities between for `Boat`s and `Sub`s, and so on. So we'd get a hierarchy more like:

```
Vehicle
  |_____LandVehicle
  |          |_______Bicycle
  |          |_______Car
  |          |_______Truck
  |
  |_____SeaVehicle
  |          |_______Sub
  |          |_______Boat
  |
  |_____AirVehicle
             |_______Plane
             |_______Helicopter
```
This is all well and good as we get to avoid lots of code duplication. There is however a problem. What do we do about sea planes, or duck boats for that matter? These classes would naturally incorporate multiple categories of vehicle, i.e. `SeaPlan` should extend from both `SeaVehicle` and `AirVehicle`. Since Java doesn't have multiple inheritance we'd be left needing to do some code duplication. Also we'd likely want to introduce interfaces so that a `SeaPlane` could be used as either a `SeaVehicle` or `LandVehicle`. 

Yikes, so it looks like the above hierarchy should be defined as interfaces, and then we need a second duplicate hierarchy of classes which implement those interfaces, and then some oddball custom implementations for these weird types of vehicles which fall into multiple categories, and again for those we'd end up duplicating some of the logic and state. Then of course someone will come up with a `FlyingBicycleSub` and we need further duplications.

So that brings us back to mixins. Mixins allow you to inherit both logic and state, and are not limited to single inheritance. If Java had mixins we'd simply adjust the above hierarchy as follows:

```
Vehicle
  |_____LandVehicle
  |          |_______Bicycle
  |          |_______Car
  |          |_______Truck
  |                       \_______DuckBoat
  |_____SeaVehicle        / 
  |          |_______Sub /
  |          |_______Boat
  |                      \________SeaPlane
  |_____AirVehicle       /
             |_______Plane
             |_______Helicopter
```

This would be nice, we'd be back to a single hierarchy and `DuckBoat`s and `SeaPlane`s would simply inherit (actually incorporate) multiple mixins in order to pick up their logic and state without needing to repeat all sorts of code.

## Java: Incorporating Mixins

Now lets look at how we can actually achieve the above with Java.

Our Java mixins are defined via interfaces with `default` methods to provide their logic, and these mixin interfaces extend a special `Mixin` interface. A class which wishes to "incorporate" a mixin needs to just include it in its `implements` clause. 

For example lets define a `DuckBoat` as a class mixing in `Truck` and `Boat` mixins.

```
class DuckBoat
    extends Mixin.Base implements Truck, Boat
    {
    // nothing more needed here
    }
```

That's it, we now have a functional `DuckBoat` which exposes all the logic as well as state from `Truck`, `Boat`, `LandVehicle`, `SeaVehicle`, and `Vehicle`, and we can write code like:

```
DuckBoat quackers = new DuckBoat();

quackers.setSeats(24);
quackers.setWheels(6);
quackers.setDoors(2);
quackers.setProps(2);
quackers.setDisplacement(1234);
```

And of course `quackers` can be passed to methods which expect either a land or sea vehicle.

So how did we pick up the state? As you probably noticed we actually did more than just declare that we `implement` some mixins, we also declared that we `extend` a special `Mixin.Base` class. `Mixin.Base` is what is managing the state for the various mixins. If your class already inherits from some other class you'll need to do a bit more work to enable mixins.

```
class DuckBoat
    extends SomeNonMixinCapableClass implements Truck, Boat
    {
    private final Mixin.State mixin = Mixin.State.of(this);
    public final Mixin.State mixin() {return mixin;}
    }
```
That's all that is required, and now `DuckBoat` can incorporate as many mixins as it likes without needing to write any additional boilerplate code. Any class extending `DuckBoat` can add further incorporations without writing any boilerplate of their own. What we see above is the literal implementation of `Mixin.Base`. By the way the `mixin()` method is declared on the `Mixin` interface, so here we're just providing the implementaiton, and if we forget to the compiler will complain. Also note that we made our implementation `final` so that any class which extends `DuckBoat` won't accidently repeat our work and define needless additional copies of the state.

That is all that you need to know in order to make use of mixins, but the question remains, how do you write a mixin?

## Java: Authoring Mixins

As it turns out authoring a new mixin is similarly easy, and not much harder than implementing a traditional class.

Let's start by writing our `Vehicle` mixin:

```
interface Vehicle extends Mixin
    {
    default int getSeats()
        {
        return mixin(State.class).seats;
        }
        
    default void setSeats(int seats)
        {
        mixin(State.class).seats = seats;
        }
        
    default int getStorageCapacityFt3()
        {
        return mixin(State.class).storageCapacityFt3;
        }
        
    default void setStorageCapacityFt3(int storageCapacityFt3)
        {
        mixin(State.class).storageCapacityFt3 = storageCapacityFt3;
        }
        
    final class State extends Mixin.State
        {
        private int seats;
        private int storageCapacityFt3;
        }        
    }
```

We've defined `Vehicle` as an interface extending the `Mixin` interface and provided defaults for the method it declares.  In order to store and access state for a `Vehicle` we use a `mixin(State.class)` prefix. This can be thought of as similar to `this` on a `class`'s method declaration. What `mixin(State.class)` does is find the `Vehicle.State` instance within a particular `Vehicle` instance. `Vechicle.State` is just a normal class which has our fields. You may notice that as compared to our original `class` based `Vehicle` the line count is nearly identical, we've just added the line `final class State extends Mixin.State` and its `{}`. Other than that the transformation consisted of replacing `public` with `default` and `this` with `mixin(State.class).`

Note that with this pattern we mark our inner `State` class as `final`. This `final` ensures that we don't recreate the "diamond problem" when one mixin extends another (more below).

When authoring more complex mixins you may choose to simplify the method implementations a bit by introducing a `private` interface helper method to your mixin:

```
interface Vehicle extneds Mixin
    {
    default int getSeats()
        {
        return state().seats;
        }
        
    default void setSeats(int seats)
        {
        state().seats = seats;
        }
  
    default int getStorageCapacityFt3()
        {
        return state().storageCapacityFt3;
        }
        
    default void setStorageCapacityFt3(int storageCapacityFt3)
        {
        state().storageCapacityFt3 = storageCapacityFt3;
        }

    private State state() // <----- helper
        {
        return mixin(State.class);
        }
                            
    final class State extends Mixin.State
        {
        private int seats;
        private int storageCapacityFt3;
        }        
    }
```

This `private` method is also quite useful if your mixin will have type parameters. For example:

```
interface ValueHolder<V> extends Mixin
    {
    default V getValue()
        {
        return state().value;
        }
        
    default void setValue(V value)
        {
        state().value = value;
        }
        
    @SupressWarnings("unchecked")
    private State<V> state()
        {
        return mixin(State.class);
        }
        
    final class State<V> extends Mixin.State
        {
        private V value;
        }    
    }
```

Now let's extend our `Vehicle` mixin to make `LandVehicle`. As mentioned previously we want to avoid the "diamond problem", but that doesn't mean we can't use interface inheritance, we just need to not use `State` inheritance:

```
interface LandVehicle extends Vehicle
    {
    default int getWheels()
        {
        return state().wheels;
        }
        
    default void setWheels(int wheels)
        {
        state().wheels = wheels;
        }
        
    private State state()
        {
        return mixin(State.class);
        }        
        
    final class State extends Mixin.State // <-- doesn't extend Vehicle.State
        {
        private int wheels;
        }    
    }
```

Note we've made `LandVehicle` extend `Vehicle` and thus inherited all of its behavior, but `LandVehicle.State` has no relationship to `Vehicle.State`. When a `DuckBoat` is instantiated it will have exactly one copy of each inner `State` object, and when `LandVechile` asks for its `State` it will receive the `LandVehicle.State` while `Vehicle`'s methods will receive `Vehicle.State`. `LandVehicle` can call the `public` methods it inherited from `Vehicle` to interact with its state as needed. If we wanted deeper integration we could put these two mixins in the same package, and make the fields package private, thus allowing `LandVehicle`'s methods to directly access `Vehicle`'s state via `mixin(Vehicle.State.class)`. 

That's really all there is to it. From the user's perspective using a `DuckBoat` is no different than using any other object, there is no reason for them to even be aware that it is mixin based. For the `DuckBoat` author incorporating mixins was trivial requiring at most a few lines of boilerplate, and for the mixin author, writing a mixin required a few simple transformations as compared to having written it as a `class.

## Mixin Mechanics

As to how this all works under the covers it all comes back to the `Mixin.State.of(Mixin target)` method. This method constructs a `Mixin.State` object which is backed by a combination of all the derived `Mixin.State`s that are incorporated for type of the specified `target`. The corresponding `mixin(State.class)` method knows how to extract the requested state accordingly. This is not simply some `Map<Class<State>, State>` being tacked onto each incorporating object. It is far more efficient in terms of both CPU and memory overhead. The overhead is essentially, one `State` object per incorporated mixin plus an additional indirection to obtain that state. In terms of hard numbers, that is around 16 bytes of additional overhead per incorporation, and well under a nanosecond of added indirection time per incorporated mixin.

For an in depth look at the internals see [Lets Mix it Up](https://markfalco.wordpress.com/2023/04/03/10-years-later-lets-mix-it-up/).
