package org.xtclang.jmixin;


import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
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

    interface IndexedList<V> extends Mixin, List<V>
        {
        @Override
        default V get(int index)
            {
            State<V> state = state();
            for (;;)
                {
                try
                    {
                    if (state.iter == null)
                        {
                        state.iter = listIterator(index);
                        state.index = index;
                        state.prior = state.iter.next();
                        }

                    if (index == state.index)
                        {
                        return state.prior;
                        }
                    else if (index > state.index)
                        {
                        do
                            {
                            state.prior = state.iter.next();
                            }
                        while (index > ++state.index);

                        return state.prior;
                        }
                    else
                        {
                        do
                            {
                            state.prior = state.iter.previous();
                            }
                        while (index < state.index--);

                        return state.prior;
                        }
                    }
                catch (NoSuchElementException e)
                    {
                    throw new IndexOutOfBoundsException();
                    }
                catch (ConcurrentModificationException e)
                    {
                    state.iter = null;
                    // retry
                    }
                }
            }

        @SuppressWarnings("unchecked")
        private State<V> state()
            {
            return mixin(State.class);
            }

        final class State<V> extends Mixin.State
            {
            private ListIterator<V> iter;
            V prior;
            private int index;
            }
        }

    static class IndexedLinkedList<V>
        extends LinkedList<V> implements IndexedList<V>
        {
        private final Mixin.State mixin = Mixin.State.of(this);
        @Override
        public final Mixin.State mixin()
            {
            return mixin;
            }

        @Override
        public V get(int index)
            {
            return IndexedList.super.get(index);
            }
        }

    @Test
    void shouldMixinToList()
        {
        List<Integer> list = new IndexedLinkedList<>();

        for (int i = 0; i < 10; ++i)
            {
            list.add(i);
            }

        assertEquals(0, list.get(0));
        assertEquals(5, list.get(5));
        assertEquals(5, list.get(5));
        assertEquals(3, list.get(3));
        assertEquals(9, list.get(9));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(10));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));

        list.add(10, 10);
        assertEquals(10, list.get(10));
        }
    }
