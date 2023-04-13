package org.xtclang.jmixin;


import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for {@link Mixin}.
 *
 * @author mf
 */
public class MixinTests
    {
    interface LongIdentityMixin extends Mixin
        {
        default long getIdentity()
            {
            return mixin(State.class).id;
            }

        final class State extends Mixin.State
            {
            private static final AtomicLong ID_GENERATOR = new AtomicLong();
            private final long id = ID_GENERATOR.incrementAndGet();
            }
        }

    interface NamedMixin extends Mixin
        {
        default String getName()
            {
            return mixin(State.class).name;
            }

        default void setName(String name)
            {
            mixin(State.class).name = name;
            }

        final class State extends Mixin.State
            {
            private String name;
            }
        }

    interface UuidAbleMixin extends Mixin
        {
        default UUID getUUID()
            {
            return mixin(State.class).uuid;
            }

        class State extends Mixin.State
            {
            final UUID uuid = UUID.randomUUID();
            }
        }

    static class Example extends Mixin.Base implements LongIdentityMixin, NamedMixin, UuidAbleMixin
        {
        Example(String name)
            {
            setName(name);
            }
        }


    @Test
    void shouldProduceId()
        {
        Example a = new Example("mark");
        Example b = new Example("falco");

        assertEquals("mark", a.getName());
        assertEquals("falco", b.getName());
        assertNotEquals(a.getIdentity(), b.getIdentity());
        assertNotEquals(a.getUUID(), b.getUUID());
        }
    }
