package risch.ezinject;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 * Created by johan.risch on 21/08/15.
 */
public class EZInjectTest extends TestCase {

    public void testBind() throws Exception {

    }

    public void testThatBindThrowsExceptionWhenClassPassedInsteadOfInterface() throws Exception {
        boolean exceptionThrown = false;
        try {
            EZInject.bind(TestClass.class).to(TestImpl.class);
        } catch (RuntimeException ex) {
            exceptionThrown = ex.getMessage().contains(EZInject.INJECT_BIND_MUST_BIND_INTERFACE);
        } finally {
            assertTrue(exceptionThrown);
        }
    }

    public void testThatBindToThrowsExceptionWhenInterfaceIsPassedInsteadOfClass() throws Exception {
        boolean exceptionThrown = false;
        try {
            EZInject.bind(ITest.class).to(ITest.class);
        } catch (RuntimeException ex) {
            exceptionThrown = ex.getMessage().contains(EZInject.INJECT_BIND_TO_MUST_BE_CLASS);
        } finally {
            assertTrue(exceptionThrown);
        }
    }

    public void testImplementsInterface() {
        assertTrue(Bind.implementsInterface(TestImpl.class, ITest.class));
    }


    public void testThatBoundClassImplementsInterface() throws Exception {
        boolean exceptionThrown = false;
        try {
            EZInject.bind(ITest.class).to(TestClass.class);
        } catch (RuntimeException ex) {
            exceptionThrown = ex.getMessage().contains(EZInject.INJECT_BOUND_CLASS_MUST_IMPLEMENT_INTERFACE);
        } finally {
            assertTrue(exceptionThrown);
        }
    }

    public void testThatInjectWithoutDeclaredConstructorReturnsNewInstance() {
        EZInject.bind(ITest.class).to(TestImpl.class);
        Object o = EZInject.create(ITest.class);
        assertTrue(o instanceof TestImpl);
    }


    public void testThatInjectWithDependenciesWork() {
        EZInject.reset();
        EZInject.bind(ITest.class).to(TestImpl.class);
        EZInject.bind(ITest2.class).to(TestImpl2.class);
        EZInject.bind(ITest3.class).to(TestImpl3.class);
        EZInject.bind(ITestDep.class).to(TestImplDependencies.class);

        ITestDep o = EZInject.create(ITestDep.class);
        assertTrue(o instanceof TestImplDependencies);
        TestImplDependencies d = (TestImplDependencies) o;
        assertTrue(d.one instanceof TestImpl);
        assertTrue(d.two instanceof TestImpl2);
        assertTrue(d.three instanceof TestImpl3);
        EZInject.reset();
    }

    public void testExceptionThrownWhenNoInjectAnotationOnConstructors() {
        boolean exceptionThrown = false;
        EZInject.reset();
        try {
            EZInject.bind(ITest.class).to(TestImplNoAnnotation.class);
            EZInject.create(ITest.class);
        } catch (RuntimeException ex) {
            exceptionThrown = ex.getMessage().contains(EZInject.NO_INJECT_CONSTRUCTOR);
        }
        assertTrue(exceptionThrown);
    }


    public void testSingleton() {
        EZInject.reset();
        EZInject.bind(ITest.class).to(TestImplSingleton.class);
        ITest one = EZInject.create(ITest.class);
        ITest two = EZInject.create(ITest.class);
        assertTrue(one == two);
    }

    public void testCycleDetection() {
        boolean exceptionThrown = false;
        EZInject.reset();
        try {
            EZInject.bind(ITestA.class).to(TestA.class)
                    .bind(ITestB.class).to(TestB.class)
                    .bind(ITestC.class).to(TestC.class)
                    .bind(ITestD.class).to(TestD.class)
                    .bind(ITestE.class).to(TestE.class);
            EZInject.create(ITestA.class);
        } catch (RuntimeException ex) {
            exceptionThrown = ex.getMessage().contains(EZInject.FOUND_CYCLIC_DEPENDENCY_STRING);
        }
        assertTrue(exceptionThrown);
    }

    public void testBindInstance() {
        EZInject.reset();
        EZInject.bind(Integer.class).to(new Integer(100));
        EZInject.bind(ITest.class).to(TestBindInstance.class);
        EZInject.create(ITest.class);
        EZInject.bind(Integer.class).to(new Integer(200));
        EZInject.bind(ITest.class).to(TestBindInstance2.class);
        EZInject.create(ITest.class);
    }

    public void testInjectFieldsInClass(){
        EZInject.reset();
        EZInject.bind(ITest.class).to(TestImpl.class);
        EZInject.bind(ITest2.class).to(TestImpl2.class);
        new TestInjectFields();
    }

    public void testCustomScope() {
        EZInject.reset();
    }

    public static class TestInjectFields {
        @Inject
        ITest test;

        @Inject
        ITest2 test2;

        public TestInjectFields(){
            EZInject.inject(this);
            Assert.assertNotNull(test);
            Assert.assertNotNull(test2);
        }
    }


    public static class TestBindInstance implements ITest{
        @Inject
        public TestBindInstance(Integer i){
            assertEquals(i,new Integer(100));
        }
    }
    public static class TestBindInstance2 implements ITest{
        @Inject
        public TestBindInstance2(Integer i){
            assertEquals(i,new Integer(200));
        }
    }

    public interface ITest {

    }

    public interface ITest2 {

    }

    public interface ITest3 {

    }

    public interface ITestDep {

    }

    public static class TestImpl implements ITest {

        @Inject
        public TestImpl() {

        }
    }

    public static class TestImplSingleton implements ITest {

        @Singleton
        @Inject
        TestImplSingleton() {
        }
    }

    public static class TestImplNoAnnotation implements ITest {

    }

    public static class TestImpl2 implements ITest2 {
        @Inject
        public TestImpl2() {

        }
    }

    public static class TestImpl3 implements ITest3 {
        @Inject
        public TestImpl3() {

        }
    }


    public static class TestImplDependencies implements ITestDep {
        private final ITest one;
        private final ITest2 two;
        private final ITest3 three;

        @Inject
        public TestImplDependencies(ITest one, ITest2 two, ITest3 three) {
            this.one = one;
            this.two = two;
            this.three = three;

        }
    }

    public static class TestClass {

    }


    public static class TestImpl1Const implements ITest {
        @Inject
        public TestImpl1Const(String s) {
            System.out.println("Created ");

        }

    }

    public static class TestImpl2Const implements ITest {

        public TestImpl2Const(String s) {
            System.out.print("Created " + s);
        }

        @Inject
        public TestImpl2Const(int s) {
            System.out.print("Created " + s);
        }

    }


    public interface ITestA {

    }

    public interface ITestB {

    }

    public interface ITestC {

    }

    public interface ITestD {

    }

    public interface ITestE {

    }

    public static class TestA implements ITestA{
        @Inject
        TestA(ITestB testb, ITestC testc) {
        }

    }

    public static class TestB implements ITestB{
        @Inject
        TestB(ITestD testd, ITestE teste) {
        }
    }

    public static class TestC implements ITestC{
        @Inject
        TestC(ITestD testD) {
        }
    }

    public static class TestD implements ITestD{
        @Inject
        public TestD() {
        }
    }

    public static class TestE implements ITestE{
        @Inject
        public TestE(ITestA testa){
        }
    }


}