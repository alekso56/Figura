package org.moon.figura.newlua.test;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.newlua.LuaTypeManager;

public class LuaTest {

    public static void main(String[] args) {

        Globals globals = new Globals();
        globals.load(new JseBaseLib());
        globals.load(new PackageLib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new JseMathLib());

        LoadState.install(globals);
        LuaC.install(globals);

        LuaTypeManager typeManager = new LuaTypeManager();

        typeManager.generateMetatableFor(TestClass.class);
        typeManager.generateMetatableFor(MyFunClass.class);
        LuaValue testValue = typeManager.wrap(new TestClass());
        globals.set("myTestValue", testValue);

        globals.set("print", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                System.out.println(arg.toString());
                return LuaValue.NIL;
            }
        });

        LuaValue chunk = globals.load("""
                
                print("Hello")
                
                print(myTestValue)
                
                local fun = myTestValue.get(5, 6)
                print(fun:getA().." + "..fun:getB().." = "..myTestValue.add(fun))
                                
                """, "main");

        long time = System.nanoTime();
        chunk.call();
        time = System.nanoTime() - time;
        System.out.println(time / 1000000 + " ms");
    }


    @LuaWhitelist
    public static class TestClass {
        @LuaWhitelist
        public static double add(MyFunClass fun) {
            return fun.a + fun.b;
        }

        @LuaWhitelist
        public static MyFunClass get(int a, double b) {
            MyFunClass result = new MyFunClass();
            result.a = a;
            result.b = b;
            return result;
        }

    }

    @LuaWhitelist
    public static class MyFunClass {

        int a;
        double b;

        @LuaWhitelist
        public int getA() {
            return a;
        }

        @LuaWhitelist
        public double getB() {
            return b;
        }

    }

}
