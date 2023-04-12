package org.xtclang.examples;

import org.junit.jupiter.api.Test;
import org.xtclang.jmixin.Mixin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


/**
 * WIP: Examples of {@link org.xtclang.jmixin.Mixin}.
 *
 * @author falcom
 */
public class MixinExample
    {
    interface LongIdentityMixin extends Mixin
        {
        default long getId()
            {
            return State.get(this, State.class).id;
            }

        final class State extends Mixin.State
            {
            private static final AtomicLong ID_GENERATOR = new AtomicLong();
            private final long id = ID_GENERATOR.incrementAndGet();
            }
        }


    interface Mammal
        {
        int getArmCount();
        int getLegCount();
        boolean hasTail();
        }

    interface Pet extends Mammal
        {
        String getBreed();
        Human getOwner();
        }

    interface Named
        {
        String getName();
        }

    interface NamedMixin extends Named, Mixin
        {
        default String getName()
            {
            return State.get(this).name;
            }

        default void setName(String name)
            {
            State.get(this).name = name;
            }

        class State extends Mixin.State
            {
            private static State get(NamedMixin mixin)
                {
                return get(mixin, State.class);
                }

            private String name;
            }
        }

    interface ParentMixin<C extends Named> extends Mixin
        {
        default C addChild(C child)
            {
            State.get(this).childenByName.put(child.getName(), child);
            return child;
            }

        default Collection<C> getChildren()
            {
            return State.get(this).childenByName.values();
            }

        final class State<C extends Named> extends Mixin.State
            {
            private static <C extends Named> State<C> get(ParentMixin<C> target)
                {
                return get(target, State.class);
                }

            private final Map<String, C> childenByName = new HashMap<>();
            }
        }

    class Human extends Mixin.Base implements Mammal, NamedMixin, ParentMixin<Human>
        {
        @Override
        public int getArmCount()
            {
            return 2;
            }

        @Override
        public int getLegCount()
            {
            return 2;
            }

        @Override
        public boolean hasTail()
            {
            return false;
            }

        @Override
        public String toString()
            {
            return getName();
            }
        }

    class Catalog<T> extends Mixin.Base implements NamedMixin, ParentMixin<Catalog<T>>
        {
        final T value;

        Catalog(String name, T value)
            {
            this.value = value;
            setName(name);
            }

        Catalog(T value)
            {
            this.value = value;
            setName(value.toString());
            }

        T getValue()
            {
            return value;
            }
        }

    @Test
    void test()
        {
        Human mark = new Human();
        mark.setName("Mark");
        Human erika = new Human();
        erika.setName("Erika");
        Human mia = new Human();
        mia.setName("Mia");

        mark.addChild(erika);
        mark.addChild(mia);

        System.out.println(mark.getChildren());


        Catalog<String> computers = new Catalog<>("computers");
        Catalog<String> laptops = computers.addChild(new Catalog<>("laptops"));
        Catalog<String> desktops = computers.addChild(new Catalog<>("desktops"));

        Catalog<String> kitt = laptops.addChild(new Catalog<>("kitt"));
        kitt.addChild(new Catalog<>("cpu", "m2"));
        kitt.addChild(new Catalog<>("color", "midnight"));
        kitt.addChild(new Catalog<>("ram", "24g"));

        Catalog<String> wopr = desktops.addChild(new Catalog<>("wopr"));
        wopr.addChild(new Catalog<>("cpu", "i5"));
        wopr.addChild(new Catalog<>("color", "silver"));
        wopr.addChild(new Catalog<>("ram", "8g"));
        }
    }
