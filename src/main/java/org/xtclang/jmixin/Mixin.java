package org.xtclang.jmixin;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A base interface for building mixins, i.e. stateful interfaces.
 *
 * <p>For classes which wish to incorporate one or more mixins in addition to declaring that they {@code implement} the
 * mixin, they must also include a single field of type {@link Mixin.State} and expose it by implementing the
 * {@link #mixin()} method (preferably marking both {@code final}). Beyond that the class does not need to provide any
 * implementation logic for the mixins it incorporates. For example, assuming there is a mixin named {@code
 * BirthdayMixin}, it can be incorporated as follows:
 *
 * <pre>{@code
 * class Person implements BirthdayMixin {
 *     private final Mixin.State mixin = Mixin.State.of(this);
 *     public final Mixin.State mixin() {return mixin;}
 * }
 * }</pre>
 *
 * <p>Alternatively if the incorporating class has no base class it can extend {@link Mixin.Base} and simply inherit
 * these boilerplate declarations:
 *
 * <pre>{@code
 * class Person extends Mixin.Base implements BirthdayMixin {
 * }
 * }</pre>
 *
 * <p>In either case, incorporating additional mixins only requires adding them to the {@code implements} clause.
 *
 * <p>Mixin interfaces consist of {@code default} methods and an inner {@code State} class which derives from {@link
 * Mixin.State}. These methods access their state via a parameterized {@link #mixin(Class) mixin(State.class)} method,
 * simply passing their derived {@code State.class}.
 *
 * <pre>{@code
 * interface BirthdayMixin extends Mixin {
 *     default Date getBirthday() {
 *         return mixin(State.class).birthday;
 *     }
 *
 *     final class State extends Mixin.State {
 *         private final Date birthday = new Date();
 *     }
 * }
 * }</pre>
 *
 * <p>{@link Mixin}s may choose to define a {@code private} helper method to make their internal state access even easier.
 * In addition to simplifying the usage, it also helps when writing mixins with type parameters.
 *
 * <pre>{@code
 * interface SomeGenericMixin<V> extends Mixin {
 *
 *     default V getValue() {
 *         return state().value;
 *     }
 *
 *     default void setValue(V value) {
 *         state().value = value;
 *     }
 *
 *     @SuppressWarnings("unchecked")
 *     private State<V> state() {
 *         return mixin(State.class);
 *     }
 *
 *     final class State<V> extends Mixin.State {
 *         private V value;
 *     }
 * }
 * }</pre>
 *
 * <p>When authoring a mixin it is allowable to extend other mixins, but not advisable to extend their state class as that
 * recreates the classic "diamond problem" where multiple disjoint copies of the state would exist. Instead, the
 * inherited mixin's state may be accessed either through the interface methods, or via {@link #mixin(Class)} assuming
 * they've chosen to not make those fields {@code private}.
 *
 * <p>A mixin's inner {@link State} is allowed to have a parameterized constructor, but if it does then any incorporating
 * class will need to do a bit more work to call it. Let's revise our {@code BirthdayMixin} such that the birthday can
 * be supplied via a parameterized constructor.
 *
 * <pre>{@code
 * interface BirthdayMixin extends Mixin {
 *     default Date getBirthday() {
 *         return mixin(State.class).birthday;
 *     }
 *
 *     final class State extends Mixin.State {
 *         private final Date birthday;
 *
 *         public State(Date birthday) {
 *             this.birthday = birthday;
 *         }
 *
 *         public State() {
 *             this(new Date());
 *         }
 *     }
 * }
 * }</pre>
 *
 * This new {@code BirthdayMixin} works the same as before and our {@code Person} class doesn't <em>require</em> any
 * changes. But if {@code Person} wants to override the default {@code birthday} then it would need to be updated as
 * follows:
 *
 * <pre>{@code
 * class Person extends Mixin.Base implements BirthdayMixin {
 *     public Person(Date birthday) {
 *         mixin(new BirthdayMixin.State(birthday));
 *     }
 * }
 * }</pre>
 *
 * Note that mixing in the {@code State} in this form should be done as part of construction, and can only be done once
 * per {@link Mixin.State} type (i.e. you can't have two birthdays), and can only be done for {@link Mixin.State}s which
 * were declared as having been incorporated (via the {@code implements} clause). If a method on that mixin is invoked
 * before its state has been specified a state will be lazily incorporated via the state's {@code public} zero-argument
 * constructor, if available, otherwise a {@code null} state and {@code NullPointerException} will result. For these
 * reasons it is good practice to include a default {@code public} zero-argument constructor when possible, and to
 * incorporate via a parameterized constructor within the incorporating class's constructor.
 *
 * @author mf
 */
public interface Mixin
    {
    /**
     * Return the pre-allocated {@link Mixin.State} for this object as allocated by {@link Mixin.State#of(Mixin)}.
     *
     * <p>This is the only interface method which must be implemented by the class incorporating mixins, and it must
     * return the value of a {@link Mixin.State} field.
     *
     * <p>This consists of the following:
     * <pre>{@code
     * class SomeClassIncorporatingMixins implements SomeMixin, MaybeSomeOtherMixin {
     *     protected final Mixin.State mixin = Mixin.State.of(this);
     *     public final Mixin.State mixin() {return mixin;}
     * }
     * }</pre>
     *
     * @return the {@link Mixin.State}
     */
    Mixin.State mixin();

    /**
     * Return the {@link Mixin.State} for the given derived {@link Mixin.State} type.
     *
     * <p>This method is used internally by {@link Mixin}s in order to access their state and does not need to be
     * overridden.
     *
     * @param clzState the state to get
     *
     * @return the state
     *
     * @param <S> the derived state type
     */
    default <S extends Mixin.State> S mixin(Class<S> clzState)
        {
        return mixin().get(clzState);
        }

    /**
     * Initialize an incorporating object with specific {@link Mixin.State} instance. This is needed when the
     * {@link Mixin.State}s have parameter based constructors. This method if called but be invoked as part of the
     * construction of the incorporating object. It may be called multiple times across the incorporating object's
     * initialization hierarchy, but can only be called once with each {@link Mixin.State}.
     *
     * @param state the state
     */
    default void mixin(Mixin.State state)
        {
        mixin().mixin(state);
        }

    /**
     * An optional base class for classes which incorporate {@link Mixin}s.
     *
     * <p>Extending this class is optional and if using it as a base class is not possible then the implementing can
     * simply implement the {@link #mixin()} method as described in its documentation.
     */
    abstract class Base
            implements Mixin
        {
        /**
         * The state for all incorporated {@link Mixin}s.
         */
        private final Mixin.State mixin = Mixin.State.of(this);

        @Override
        public final Mixin.State mixin()
            {
            return mixin;
            }
        }

    /**
     * Base class for a {@link Mixin}'s internal {@code State} implementations.
     *
     * <p>Each {@link Mixin} interface should declare an inner class which derives from {@link State}. This derived state
     * can be accessed via {@link Mixin#mixin(Class)}, passing the derived {@code State.class}.
     *
     * <p>Typically the derived class will be {@code final} and its fields will be {@code private}. This ensures that
     * the state cannot be derived (creating the diamond problem), and that it can only be accessed within the enclosing
     * {@link Mixin} interface.
     */
    class State
        {
        /**
         * {@link VarHandle} for working with an array of {@link State}s.
         */
        private static final VarHandle STATES = MethodHandles.arrayElementVarHandle(State[].class);

        /**
         * Allocate the {@link State} to be stored in a {@code protected final} field of a class which incorporates mixins.
         *
         * @param target the object incorporating mixins
         * @return the {@link State}
         */
        public static @NotNull State of(@NotNull Mixin target)
            {
            return ALLOC.get(target.getClass()).get();
            }

        /**
         * Return the {@link State} specific to the specified {@link Mixin.State} derived type.
         *
         * @param clzState the derived {@link Mixin.State} type
         * @param <S>      the derived state type
         * @return the state
         * @throws RuntimeException if {@code clzState} is not a {@link Mixin.State} for one of the {@link Mixin}s
         *                          implemented by the class on which it is invoked.
         */
        @SuppressWarnings("unchecked")
        /*package*/ <S extends State> S get(@NotNull Class<S> clzState)
            {
            return (S) this;
            }

        /**
         * Late mixin a state object
         *
         * @param state the state to mix-in
         */
        /*package*/ void mixin(@NotNull State state)
            {
            throw new UnsupportedOperationException();
            }

        /**
         * A {@link ClassValue} keyed by the classes which incorporate {@link Mixin}s. The stored value is an "allocator"
         * for the {@link State} implementation for that specific class. That {@link State} will contain the individual
         * derived {@link State} objects for each of the incorporated {@link Mixin}s.
         */
        private static final ClassValue<Supplier<State>> ALLOC = new ClassValue<>()
            {
            @Override
            protected Supplier<State> computeValue(Class<?> clz)
                {
                boolean deferred = false;
                IdentityHashMap<Class<State>, Supplier<State>> allocMap = new IdentityHashMap<>();
                for (Class<Mixin> mixin : getMixins(clz, new HashSet<>()))
                    {
                    // find the mixin's state class and constructor
                    for (Class<?> clzInner : mixin.getDeclaredClasses())
                        {
                        if (clzInner != State.class && State.class.isAssignableFrom(clzInner))
                            {
                            @SuppressWarnings("unchecked")
                            Class<State> clzState = (Class<State>) clzInner;
                            try
                                {
                                Constructor<?>[] ctors = clzState.getConstructors();
                                Constructor<State> ctorZero = clzState.getConstructor();
                                Supplier<State> sup = () ->
                                    {
                                    try
                                        {
                                        return ctorZero.newInstance();
                                        }
                                    catch (Exception e)
                                        {
                                        throw new RuntimeException(e);
                                        }
                                    };


                                deferred |= ctors.length > 1;
                                allocMap.put(clzState, sup);
                                }
                            catch (NoSuchMethodException e)
                                {
                                deferred = true;
                                // no zero param constructor
                                allocMap.put(clzState, () -> null); // deferred, incorporator will need to use mixin(new State(...))
                                }
                            }
                        }
                    }

                // determine storage indexes
                IdentityHashMap<Class<State>, Integer> indexMap = new IdentityHashMap<>();
                int index = 0;
                for (Class<State> clzState : allocMap.keySet())
                    {
                    indexMap.put(clzState, index++);
                    }

                // populate stateAllocs in the same order as the indexMap
                ArrayList<Supplier<State>> stateAllocs = new ArrayList<>();
                for (Class<State> clzState : indexMap.keySet())
                    {
                    stateAllocs.add(allocMap.get(clzState));
                    }

                int cMixins = stateAllocs.size();
                var iterClz = indexMap.keySet().iterator();

                // Record the above state into a single object such that the State dispatching object we create
                // below can retain just a single ref to a StateInfo helping to minimize its overall size
                class StateInfo
                    {
                    // for common incorporating classes with just a handful of Mixins we can avoid array iteration by
                    // directly accessing these class identifiers
                    final IdentityHashMap<Class<State>, Integer> indicies = indexMap;
                    final int count = indicies.size();
                    final Class<? extends State> class0 = iterClz.hasNext() ? iterClz.next() : null;
                    final Class<? extends State> class1 = iterClz.hasNext() ? iterClz.next() : null;
                    final Class<? extends State> class2 = iterClz.hasNext() ? iterClz.next() : null;
                    final Class<? extends State> class3 = iterClz.hasNext() ? iterClz.next() : null;
                    final Class<? extends State> class4 = iterClz.hasNext() ? iterClz.next() : null;
                    final Class<? extends State> class5 = iterClz.hasNext() ? iterClz.next() : null;
                    final Class<? extends State> class6 = iterClz.hasNext() ? iterClz.next() : null;
                    final ArrayList<Supplier<State>> allocs = stateAllocs;

                    /**
                     * Lookup the index for a given {@link State} class.
                     *
                     * @param clzState the class of state to lookup
                     * @return the state's index
                     */
                    int index(Class<? extends State> clzState)
                        {
                        return count > 8
                                ? indexMap.get(clzState)
                                : clzState == class0 ? 0
                                : clzState == class1 ? 1
                                : clzState == class2 ? 2
                                : clzState == class3 ? 3
                                : clzState == class4 ? 4
                                : clzState == class5 ? 5
                                : clzState == class6 ? 6
                                :                      7;
                        }
                    }

                StateInfo info = new StateInfo();

                // create a State allocator based on the now known number of mixins for this type; if the number of
                // mixins is small (up to 8) then we build an allocator which will find the child mixin via a linear
                // scan; otherwise we use a full on map; for small sets the linear scan is both faster and more memory
                // efficient than the map
                if (deferred)
                    {
                    // this is the most complex State variant, it allows for States which are explicitly constructed
                    // by the incorporating class, and for lazily allocating States upon their first usage if they were
                    // not explicitly allocated by the incorporating class
                    return () -> new State()
                        {
                        final State[] states = new State[info.count];

                        @Override
                        @SuppressWarnings("unchecked")
                        public <S extends State> S get(@NotNull Class<S> clzState)
                            {
                            int index = info.index(clzState);
                            S state = (S) STATES.getVolatile(states, index);
                            return state == null || state == this ? ensure(index) : state;
                            }

                        /**
                         * Lazy allocate the state.
                         *
                         * @param index the state index
                         * @return the state
                         * @param <S> the state type
                         */
                        @SuppressWarnings("unchecked")
                        private <S extends State> S ensure(int index)
                            {
                            Supplier<State> alloc = info.allocs.get(index);
                            // don't allow concurrent ensures to race
                            synchronized (this)
                                {
                                S state = (S) STATES.getVolatile(states, index);
                                if (state == null || state == this)
                                    {
                                    // prevent a concurrent mixin(State) from winning a race; we want to ensure
                                    // that every automatic alloc is retained in case they had any side effects
                                    state = (S) STATES.compareAndExchange(states, index, state, this);
                                    if (state == null || state == this)
                                        {
                                        state = (S) alloc.get();
                                        STATES.setVolatile(states, index, state);
                                        }
                                    }
                                return state;
                                }
                            }

                        @Override
                        void mixin(@NotNull State state)
                            {
                            Integer index = info.indicies.get(state.getClass());
                            if (index == null)
                                {
                                throw new IllegalArgumentException("not a mixed in type: " + state.getClass());
                                }

                            // avoid sync and inflating the monitor on this common path
                            if (!STATES.compareAndSet(states, index, null, state))
                                {
                                throw new IllegalArgumentException("state already supplied: " + state.getClass());
                                }
                            }
                        };
                    }
                // for all others we don't need to be lazy and can allocate our States at the time of incorporation
                else if (cMixins > 8)
                    {
                    // if we have a large number of mixins we'll avoid the linear scan and instead do a map
                    // lookup; the map is per-class rather than per-instance to save memory, each instance simply
                    // has an array of states, and the indexes correspond to the indexMap's values
                    return () ->
                        {
                        // allocated outside of State inner class to avoid it capturing stateAllocs/cAllocs
                        State[] states = new State[info.count];
                        for (var alloc : info.allocs)
                            {
                            State s = alloc.get();
                            states[info.indicies.get(s.getClass())] = s;
                            }

                        return new State()
                            {
                            @Override
                            @SuppressWarnings("unchecked")
                            public <S extends State> S get(@NotNull Class<S> clzState)
                                {
                                return (S) states[info.indicies.get(clzState)];
                                }
                            };
                        };
                    }
                else if (cMixins > 4)
                    {
                    return () ->
                        {
                        State state0 = info.allocs.get(0).get();
                        State state1 = info.allocs.get(1).get();
                        State state2 = info.allocs.get(2).get();
                        State state3 = info.allocs.get(3).get();
                        State state4 = info.allocs.get(4).get();
                        State state5 = info.count <= 5 ? null : info.allocs.get(5).get();
                        State state6 = info.count <= 6 ? null : info.allocs.get(6).get();
                        State state7 = info.count <= 7 ? null : info.allocs.get(7).get();
                        return new State()
                            {
                            @Override
                            @SuppressWarnings("unchecked")
                            protected <S extends State> S get(@NotNull Class<S> clzState)
                                {
                                // only capture info, to keep the instance small
                                return (S) (clzState == info.class0 ? state0
                                          : clzState == info.class1 ? state1
                                          : clzState == info.class2 ? state2
                                          : clzState == info.class3 ? state3
                                          : clzState == info.class4 ? state4
                                          : clzState == info.class5 ? state5
                                          : clzState == info.class6 ? state6
                                          :                           state7);
                                }
                            };
                        };
                    }
                else if (cMixins > 2)
                    {
                    return () ->
                        {
                        State state0 = info.allocs.get(0).get();
                        State state1 = info.allocs.get(1).get();
                        State state2 = info.allocs.get(2).get();
                        State state3 = info.count <= 3 ? null : info.allocs.get(3).get();
                        return new State()
                            {
                            @Override
                            @SuppressWarnings("unchecked")
                            protected <S extends State> S get(@NotNull Class<S> clzState)
                                {
                                // only capture info, to keep the instance small
                                return (S) (clzState == info.class0 ? state0
                                          : clzState == info.class1 ? state1
                                          : clzState == info.class2 ? state2
                                          :                           state3);
                                }
                            };
                        };
                    }
                else if (cMixins == 2)
                    {
                    return () ->
                        {
                        // allocated outside of State inner class to avoid it capturing info
                        State state0 = info.allocs.get(0).get();
                        State state1 = info.allocs.get(1).get();
                        Class<? extends State> class0 = info.class0;
                        return new State()
                            {
                            @Override
                            @SuppressWarnings("unchecked")
                            protected <S extends State> S get(@NotNull Class<S> clzState)
                                {
                                return (S) (clzState == class0 ? state0 : state1);
                                }
                            };
                        };
                    }
                else if (cMixins == 1)
                    {
                    // most common case, single mixin, we can just allocate its state and not have to do any lookup
                    return stateAllocs.get(0);
                    }
                else
                    {
                    // no mixins
                    return () -> null;
                    }
                }
            };

        /**
         * Populate the supplied list with all {@link Mixin}s implemented by the supplied class.
         *
         * @param clz       the class to introspect
         * @param ifacesOut the list to populate
         */
        @SuppressWarnings("unchecked")
        private static Set<Class<Mixin>> getMixins(Class<?> clz, Set<Class<Mixin>> ifacesOut)
            {
            for (Class<?> iface : clz.getInterfaces())
                {
                if (Mixin.class.isAssignableFrom(iface))
                    {
                    ifacesOut.add((Class<Mixin>) iface);
                    }

                getMixins(iface, ifacesOut);
                }

            return ifacesOut;
            }
        }
    }
