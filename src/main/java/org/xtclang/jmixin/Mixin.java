package org.xtclang.jmixin;

import org.jetbrains.annotations.NotNull;

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
 * mixin, they must also include a single {@code protected final} of type {@link Mixin.State} and expose it by implementing
 * the {@link #mixin()} method. Beyond that the class does not need to provide any implementation logic for the mixins
 * it incorporates. For example, assuming there is a mixin named {@code BirthdayMixin}, it can be incorporated as follows:
 *
 * <pre>{@code
 * class Person implements BirthdayMixin {
 *     protected final Mixin.State mixin = Mixin.State.of(this);
 *     public Mixin.State mixin() {
 *         return mixin;
 *     }
 * }
 * }</pre>
 *
 * <p>If the class were to incorporate multiple mixins it would only need to add them to the {@code implements} clause.
 *
 * <p>Mixin interfaces consists of default methods and an inner {@code State} class which derives from {@link Mixin.State}.
 * The default methods access their state via the parameterized {@link #mixin(Class)} method, passing in their {@code State}
 * class.
 *
 * <pre>{@code
 * interface BirthdayMixin extends Mixin {
 *     default Date getBirthday() {
 *         return mixin(State.class).birthday;
 *     }
 *
 *     class State extends Mixin.State {
 *         private final Date birthday = new Date();
 *     }
 * }
 * }</pre>
 *
 * <p>When authoring a mixin it is allowable to extend other mixins, but not advisable to extend their state class as that
 * recreates the classic "diamond problem" where multiple disjoint copies of the state would exist. Instead, the
 * inherited mixin's state may be accessed either through the interface methods, or via {@link #mixin(Class)
 * mixin(OtherMixin.State.class)} assuming they've chosen to make those fields non-{@code private}.
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
     *     public Mixin.State mixin() {
     *         return mixin;
     *     }
     * }
     * }</pre>
     *
     * @return the {@link Mixin.State}
     */
    Mixin.State mixin();

    /**
     * Return the {@link State} specific to the specified {@link Mixin.State} derived type.
     *
     * @param clzState the derived {@link Mixin.State} type
     * @param <S>      the derived state type
     * @return the state
     */
    default <S extends State> @NotNull S mixin(@NotNull Class<S> clzState)
        {
        return mixin().get(clzState);
        }

    /**
     * Base class for mixin's to extend in order to declare their state.
     */
    class State
        {
        /**
         * Allocate the {@link State} to be stored in a {@code protected final} field of a class which incorporates mixins.
         *
         * @param mixin the object incorporating mixins
         * @return the {@link State}
         */
        public static <M extends Mixin> @NotNull State of(M mixin)
            {
            return ALLOC.get(mixin.getClass()).get();
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

        private static final ClassValue<Supplier<State>> ALLOC = new ClassValue<>()
            {
            @Override
            protected Supplier<State> computeValue(Class<?> clz)
                {
                IdentityHashMap<Class<State>, Supplier<State>> allocMap = new IdentityHashMap<>();
                for (Class<Mixin> mixin : getMixins(clz, new HashSet<>()))
                    {
                    // find the mixin's state class and constructor
                    for (Class<?> clzInner : mixin.getDeclaredClasses())
                        {
                        if (clzInner != State.class && State.class.isAssignableFrom(clzInner))
                            {
                            try
                                {
                                @SuppressWarnings("unchecked")
                                Class<State> clzState = (Class<State>) clzInner;
                                Constructor<State> ctor = clzState.getConstructor();
                                allocMap.put(clzState, () ->
                                    {
                                    try
                                        {
                                        return ctor.newInstance();
                                        }
                                    catch (Exception e)
                                        {
                                        throw new RuntimeException(e);
                                        }
                                    });
                                }
                            catch (NoSuchMethodException e)
                                {
                                throw new RuntimeException(e);
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

                var iterClz = indexMap.keySet().iterator();
                // to be captured below; capture classes in the same order as indexMap/stateAllocs
                Class<? extends State> class0 = iterClz.hasNext() ? iterClz.next() : null;
                Class<? extends State> class1 = iterClz.hasNext() ? iterClz.next() : null;
                Class<? extends State> class2 = iterClz.hasNext() ? iterClz.next() : null;
                Class<? extends State> class3 = iterClz.hasNext() ? iterClz.next() : null;
                Class<? extends State> class4 = iterClz.hasNext() ? iterClz.next() : null;
                Class<? extends State> class5 = iterClz.hasNext() ? iterClz.next() : null;
                Class<? extends State> class6 = iterClz.hasNext() ? iterClz.next() : null;
                Class<? extends State> class7 = iterClz.hasNext() ? iterClz.next() : null;

                // create a State allocator based on the now known number of mixins for this type;
                // if the number of mixins is small (up to 8) then we build an allocator which will
                // find the child mixin via a linear scan; otherwise we use a full on map; for small
                // sets the linear scan is both faster and more memory efficient than the map
                int cAllocs = stateAllocs.size();
                if (cAllocs == 0)
                    {
                    // odd case that a class implements Mixin but doesn't incorporate any Mixins (not an error)
                    return State::new;
                    }
                else if (cAllocs == 1)
                    {
                    // most common case, single mixin, we can just allocate its state and not have to do any lookup
                    return stateAllocs.get(0);
                    }
                else if (cAllocs == 2)
                    {
                    return () ->
                        {
                        // allocated outside of State inner class to avoid it capturing stateAllocs
                        State state0 = stateAllocs.get(0).get();
                        State state1 = stateAllocs.get(1).get();
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
                else if (cAllocs <= 4)
                    {
                    return () ->
                        {
                        // allocated outside of State inner class to avoid it capturing stateAllocs/cAllocs
                        State state0 = stateAllocs.get(0).get();
                        State state1 = stateAllocs.get(1).get();
                        State state2 = stateAllocs.get(2).get();
                        State state3 = class3 == null ? null : stateAllocs.get(3).get();
                        return new State()
                            {
                            @Override
                            @SuppressWarnings("unchecked")
                            protected <S extends State> S get(@NotNull Class<S> clzState)
                                {
                                // we capture the classNs rather than using stateN.getClass() to help avoid cache misses
                                return (S) (clzState == class0 ? state0
                                        : clzState == class1 ? state1
                                        : clzState == class2 ? state2
                                        : state3);
                                }
                            };
                        };
                    }
                else if (cAllocs <= 8)
                    {
                    return () ->
                        {
                        // allocated outside of State inner class to avoid it capturing stateAllocs/cAllocs
                        State state0 = stateAllocs.get(0).get();
                        State state1 = stateAllocs.get(1).get();
                        State state2 = stateAllocs.get(2).get();
                        State state3 = stateAllocs.get(3).get();
                        State state4 = stateAllocs.get(4).get();
                        State state5 = class5 == null ? null : stateAllocs.get(5).get();
                        State state6 = class6 == null ? null : stateAllocs.get(6).get();
                        State state7 = class7 == null ? null : stateAllocs.get(7).get();
                        return new State()
                            {
                            @Override
                            @SuppressWarnings("unchecked")
                            protected <S extends State> S get(@NotNull Class<S> clzState)
                                {
                                // we capture the classNs rather than using stateN.getClass() to help avoid cache misses
                                return (S) (clzState == class0 ? state0
                                        : clzState == class1 ? state1
                                        : clzState == class2 ? state2
                                        : clzState == class3 ? state3
                                        : clzState == class4 ? state4
                                        : clzState == class5 ? state5
                                        : clzState == class6 ? state6
                                        : state7);
                                }
                            };
                        };
                    }
                else
                    {
                    // if we have a large number of mixins we'll avoid the linear scan and instead do a map
                    // lookup; the map is per-class rather than per-instance to save memory, each instance simply
                    // has an array of states, and the indexes correspond to the indexMap's values
                    return () ->
                        {
                        // allocated outside of State inner class to avoid it capturing stateAllocs/cAllocs
                        State[] states = new State[cAllocs];
                        for (var alloc : stateAllocs)
                            {
                            State s = alloc.get();
                            states[indexMap.get(s.getClass())] = s;
                            }

                        return new State()
                            {
                            @Override
                            @SuppressWarnings("unchecked")
                            public <S extends State> S get(@NotNull Class<S> clzState)
                                {
                                return (S) states[indexMap.get(clzState)];
                                }
                            };
                        };
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

    /**
     * A base class for classes which incorporate {@link Mixin}s.
     *
     * <p>Extending this class is optional and if using it as a base class is not possible then the implementing can
     * simply implement the {@link #mixin()} method as described in its documentation.
     */
    class Base
            implements Mixin
        {
        protected final Mixin.State mixin = Mixin.State.of(this);

        @Override
        public State mixin()
            {
            return mixin;
            }
        }
    }
