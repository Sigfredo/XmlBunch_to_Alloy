package test;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorAPI;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

public class EvaluatorExample {

    public static void compileModuleAndRun(String model) throws Err {
        A4Reporter rep = new A4Reporter();

        // parse model from string
        CompModule world = CompUtil.parseOneModule("sig N, V {classes: set X}"
       +" sig X{}"
        +"sig A, B extends X{}"
        +"sig Rel{r: set X -> X}"
        +"fact {V.classes = A}"
        +"fact {N.classes = B}"
        +"fact {Rel.r = B -> A}"
        +"assert show{"
        + "no x: V.classes, y: N.classes | y->x in Rel.r"
        + "}"
        + "check show"
        		);
        ConstList<Command> commands = world.getAllCommands();
        if (commands.size() != 1)
            throw new ErrorAPI("Must specify exactly one command; number of commands found: " + commands.size());
        Command cmd = commands.get(0);
        A4Options opt = new A4Options();
        opt.solver = A4Options.SatSolver.SAT4J;

        // solve
        A4Solution sol = TranslateAlloyToKodkod.execute_command(rep, world.getAllSigs(), cmd, opt);

        // print solution
        System.out.println(sol);

        for (Sig sig : world.getAllReachableSigs()) {
            System.out.println("traversing sig: " + sig);
            SafeList<Field> fields = sig.getFields();
            for (Field field : fields) {
                System.out.println("  traversing field: " + field);
                A4TupleSet ts = (A4TupleSet) (sol.eval(field));
                for (A4Tuple t : ts) {
                    System.out.print("    [");
                    for (int i = 0; i < t.arity(); i++)
                        System.out.print(t.atom(i) + " ");
                    System.out.println("]");
                }                        
            }
        }
    }

    public static void main(String[] args) throws Err {
        String model = "sig A { f: set A}\n\n run { #f > 1 } for 4";
        if (args.length > 0)
            model = args[0];
        compileModuleAndRun(model);
    }

}