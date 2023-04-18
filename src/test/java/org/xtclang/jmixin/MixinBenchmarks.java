package org.xtclang.jmixin;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Microbenchmarks of {@link Mixin}.
 *
 * Java 18 on an M2 MacBook Air
 *
 * <pre>
 * Benchmark                     Mode  Cnt  Score   Error  Units
 * MixinBenchmarks.testBaseline  avgt    6  1.723 ± 0.081  ns/op
 * MixinBenchmarks.testLazy      avgt    6  2.545 ± 0.009  ns/op
 * MixinBenchmarks.testMixin1    avgt    6  1.663 ± 0.053  ns/op
 * MixinBenchmarks.testMixin2    avgt    6  1.925 ± 0.212  ns/op
 * MixinBenchmarks.testMixin3    avgt    6  2.284 ± 0.251  ns/op
 * MixinBenchmarks.testMixin4    avgt    6  2.516 ± 0.051  ns/op
 * MixinBenchmarks.testMixin5    avgt    6  2.574 ± 0.512  ns/op
 * MixinBenchmarks.testMixin6    avgt    6  2.900 ± 0.232  ns/op
 * MixinBenchmarks.testMixin7    avgt    6  2.667 ± 0.311  ns/op
 * MixinBenchmarks.testMixin8    avgt    6  2.697 ± 0.063  ns/op
 * MixinBenchmarks.testMixin9    avgt    6  4.283 ± 0.029  ns/op
 * </pre>
 *
 * @author mf
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
public class MixinBenchmarks
    {
    private static final int LIMIT = 1;

    private static int NEXT = 0;

    static int next()
        {
        return (NEXT += 17) % 9;
        }

    interface FirstMixin extends Mixin
        {
        default int first()
            {
            return mixin(State.class).value;
            }

        class State extends Mixin.State
            {
            private final int value = ThreadLocalRandom.current().nextInt();
            }
        }

    interface SecondMixin extends Mixin
        {
        default long second()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            private long value = ThreadLocalRandom.current().nextLong();
            }
        }

    interface ThirdMixin extends Mixin
        {
        default short third()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            private short value = (short) ThreadLocalRandom.current().nextInt();
            }
        }

    interface FourthMixin extends Mixin
        {
        default byte fourth()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            private byte value = (byte) ThreadLocalRandom.current().nextInt();
            }
        }

    interface FifthMixin extends Mixin
        {
        default char fifth()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            private char value = (char) ThreadLocalRandom.current().nextInt();
            }
        }

    interface SixthMixin extends Mixin
        {
        default int sixth()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            private int value = ThreadLocalRandom.current().nextInt();
            }
        }

    interface SeventhMixin extends Mixin
        {
        default int seventh()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            private int value = ThreadLocalRandom.current().nextInt();
            }
        }

    interface EighthMixin extends Mixin
        {
        default int eigth()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            private int value = ThreadLocalRandom.current().nextInt();
            }
        }

    interface NinthMixin extends Mixin
        {
        default int ninth()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            private int value = ThreadLocalRandom.current().nextInt();
            }
        }

    interface LazyMixin extends Mixin
        {
        default int lazy()
            {
            return mixin(State.class).value++;
            }

        class State extends Mixin.State
            {
            public State(int value)
                {
                this.value = value;
                }
            
            public State()
                {
                this(ThreadLocalRandom.current().nextInt());
                }
            private int value;
            }
        }

    static class Mixin1 extends Mixin.Base implements FirstMixin
        {
        }

    static class Mixin2 extends Mixin.Base implements FirstMixin, SecondMixin
        {
        }

    static class Mixin3 extends Mixin.Base implements FirstMixin, SecondMixin, ThirdMixin
        {
        }

    static class Mixin4 extends Mixin.Base implements FirstMixin, SecondMixin, ThirdMixin, FourthMixin
        {
        }

    static class Mixin5 extends Mixin.Base implements FirstMixin, SecondMixin, ThirdMixin, FourthMixin, FifthMixin
        {
        }

    static class Mixin6 extends Mixin.Base implements FirstMixin, SecondMixin, ThirdMixin, FourthMixin, FifthMixin, SixthMixin
        {
        }

    static class Mixin7 extends Mixin.Base implements FirstMixin, SecondMixin, ThirdMixin, FourthMixin, FifthMixin, SixthMixin, SeventhMixin
        {
        }

    static class Mixin8 extends Mixin.Base implements FirstMixin, SecondMixin, ThirdMixin, FourthMixin, FifthMixin, SixthMixin, SeventhMixin, EighthMixin
        {
        }

    static class Mixin9 extends Mixin.Base implements FirstMixin, SecondMixin, ThirdMixin, FourthMixin, FifthMixin, SixthMixin, SeventhMixin, EighthMixin, NinthMixin
        {
        }

    static class Lazy extends Mixin.Base implements LazyMixin
        {
        }

    static final Mixin1 mixin1 = new Mixin1();
    static final Mixin2 mixin2 = new Mixin2();
    static final Mixin3 mixin3 = new Mixin3();
    static final Mixin4 mixin4 = new Mixin4();
    static final Mixin5 mixin5 = new Mixin5();
    static final Mixin6 mixin6 = new Mixin6();
    static final Mixin7 mixin7 = new Mixin7();
    static final Mixin8 mixin8 = new Mixin8();
    static final Mixin9 mixin9 = new Mixin9();
    static final Lazy lazy = new Lazy();

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testBaseline()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += 4;
                    break;
                case 1:
                    c += 15;
                    break;
                case 2:
                    c += 42;
                    break;
                case 3:
                    c += 87;
                    break;
                case 4:
                    c += 33;
                    break;
                case 5:
                    c += 24;
                    break;
                case 6:
                    c += 49;
                    break;
                case 7:
                    c += 324;
                    break;
                default:
                    c += 88;
                    break;
                }
            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin1()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin1.first() + 42;
                    break;
                case 1:
                    c += mixin1.first() + 33;
                    break;
                case 2:
                    c += mixin1.first() + 21;
                    break;
                case 3:
                    c += mixin1.first() + 12;
                    break;
                case 4:
                    c += mixin1.first() + 88;
                    break;
                case 5:
                    c += mixin1.first() + 93;
                    break;
                case 6:
                    c += mixin1.first() + 74;
                    break;
                case 7:
                    c += mixin1.first() + 92;
                    break;
                default:
                    c += mixin1.first() + 123;
                    break;
                }
            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin2()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin2.first();
                    break;
                case 1:
                    c += mixin2.second();
                    break;
                case 2:
                    c += mixin2.first();
                    break;
                case 3:
                    c += mixin2.second();
                    break;
                case 4:
                    c += mixin2.first();
                    break;
                case 5:
                    c += mixin2.second();
                    break;
                case 6:
                    c += mixin2.first();
                    break;
                case 7:
                    c += mixin2.second();
                    break;
                default:
                    c += mixin2.first();
                    break;
                }

            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin3()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin3.first();
                    break;
                case 1:
                    c += mixin3.second();
                    break;
                case 2:
                    c += mixin3.third();
                    break;
                case 3:
                    c += mixin3.first();
                    break;
                case 4:
                    c += mixin3.second();
                    break;
                case 5:
                    c += mixin3.third();
                    break;
                case 6:
                    c += mixin3.first();
                    break;
                case 7:
                    c += mixin3.second();
                    break;
                default:
                    c += mixin3.third();
                    break;
                }

            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin4()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin4.first();
                    break;
                case 1:
                    c += mixin4.second();
                    break;
                case 2:
                    c += mixin4.third();
                    break;
                case 3:
                    c += mixin4.fourth();
                    break;
                case 4:
                    c += mixin4.first();
                    break;
                case 5:
                    c += mixin4.second();
                    break;
                case 6:
                    c += mixin4.third();
                    break;
                case 7:
                    c += mixin4.fourth();
                    break;
                default:
                    c += mixin4.first();
                    break;
                }

            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin5()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin5.first();
                    break;
                case 1:
                    c += mixin5.second();
                    break;
                case 2:
                    c += mixin5.third();
                    break;
                case 3:
                    c += mixin5.fourth();
                    break;
                case 4:
                    c += mixin5.fifth();
                    break;
                case 5:
                    c += mixin5.first();
                    break;
                case 6:
                    c += mixin5.second();
                    break;
                case 7:
                    c += mixin5.third();
                    break;
                default:
                    c += mixin5.fourth();
                    break;
                }

            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin6()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin6.first();
                    break;
                case 1:
                    c += mixin6.second();
                    break;
                case 2:
                    c += mixin6.third();
                    break;
                case 3:
                    c += mixin6.fourth();
                    break;
                case 4:
                    c += mixin6.fifth();
                    break;
                case 5:
                    c += mixin6.sixth();
                    break;
                case 6:
                    c += mixin6.fifth();
                    break;
                case 7:
                    c += mixin6.second();
                    break;
                default:
                    c += mixin6.third();
                    break;
                }

            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin7()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin7.first();
                    break;
                case 1:
                    c += mixin7.second();
                    break;
                case 2:
                    c += mixin7.third();
                    break;
                case 3:
                    c += mixin7.fourth();
                    break;
                case 4:
                    c += mixin7.fifth();
                    break;
                case 5:
                    c += mixin7.sixth();
                    break;
                case 6:
                    c += mixin7.seventh();
                    break;
                case 7:
                    c += mixin7.fifth();
                    break;
                default:
                    c += mixin7.second();
                    break;
                }

            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin8()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin8.first();
                    break;
                case 1:
                    c += mixin8.second();
                    break;
                case 2:
                    c += mixin8.third();
                    break;
                case 3:
                    c += mixin8.fourth();
                    break;
                case 4:
                    c += mixin8.fifth();
                    break;
                case 5:
                    c += mixin8.sixth();
                    break;
                case 6:
                    c += mixin8.seventh();
                    break;
                case 7:
                    c += mixin8.eigth();
                    break;
                default:
                    c += mixin8.first();
                    break;
                }
            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testMixin9()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += mixin9.first();
                    break;
                case 1:
                    c += mixin9.second();
                    break;
                case 2:
                    c += mixin9.third();
                    break;
                case 3:
                    c += mixin9.fourth();
                    break;
                case 4:
                    c += mixin9.fifth();
                    break;
                case 5:
                    c += mixin9.sixth();
                    break;
                case 6:
                    c += mixin9.seventh();
                    break;
                case 7:
                    c += mixin9.eigth();
                    break;
                default:
                    c += mixin9.ninth();
                    break;
                }
            }

        return c;
        }

    @Benchmark
    @OperationsPerInvocation(LIMIT)
    public long testLazy()
        {
        long c = 0;
        for (int i = 0; i < LIMIT; ++i)
            {
            switch (next())
                {
                case 0:
                    c += lazy.lazy() + 42;
                    break;
                case 1:
                    c += lazy.lazy() + 33;
                    break;
                case 2:
                    c += lazy.lazy() + 21;
                    break;
                case 3:
                    c += lazy.lazy() + 12;
                    break;
                case 4:
                    c += lazy.lazy() + 88;
                    break;
                case 5:
                    c += lazy.lazy() + 93;
                    break;
                case 6:
                    c += lazy.lazy() + 74;
                    break;
                case 7:
                    c += lazy.lazy() + 92;
                    break;
                default:
                    c += lazy.lazy() + 123;
                    break;
                }
            }

        return c;
        }
    }

