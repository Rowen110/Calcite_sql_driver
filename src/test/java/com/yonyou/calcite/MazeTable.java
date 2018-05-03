package com.yonyou.calcite;

import java.io.PrintWriter;
import java.util.Random;
import java.util.Set;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.linq4j.function.Function1;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Util;

public class MazeTable extends AbstractTable implements ScannableTable{

    final int width;
    final int height;
    final int seed;
    final boolean solution; 

    private MazeTable(int width, int height, int seed, boolean solution) {
      this.width = width;
      this.height = height;
      this.seed = seed;
      this.solution = solution;
    }

    /** Table function that generates a maze.
     *
     * <p>Called by reflection based on the definition of the user-defined
     * function in the schema.
     *
     * @param width Width of maze
     * @param height Height of maze
     * @param seed Random number seed, or -1 to create an unseeded random
     * @return Table that prints the maze in text form
     */
    @SuppressWarnings("unused") // called via reflection
    public static ScannableTable generate(int width, int height, int seed) {
      return new MazeTable(width, height, seed, false);
    }

    /** Table function that generates a maze with a solution.
     *
     * <p>Called by reflection based on the definition of the user-defined
     * function in the schema.
     *
     * @param width Width of maze
     * @param height Height of maze
     * @param seed Random number seed, or -1 to create an unseeded random
     * @return Table that prints the maze in text form, with solution shown
     */
    @SuppressWarnings("unused") // called via reflection
    public static ScannableTable solve(int width, int height, int seed) {
      return new MazeTable(width, height, seed, true);
    }

    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
      return typeFactory.builder()
          .add("S", SqlTypeName.VARCHAR, width * 3 + 1)
          .build();
    }

    public Enumerable<Object[]> scan(DataContext root) {
      final Random random = seed >= 0 ? new Random(seed) : new Random();
      final Maze maze = new Maze(width, height);
      final PrintWriter pw = Util.printWriter(System.out);
      maze.layout(random, pw);
      if (Maze.DEBUG) {
        maze.print(pw, true);
      }
      return new AbstractEnumerable<Object[]>() {
        public Enumerator<Object[]> enumerator() {
          final Set<Integer> solutionSet;
          if (solution) {
            solutionSet = maze.solve(0, 0);
          } else {
            solutionSet = null;
          }
          return Linq4j.transform(maze.enumerator(solutionSet),
              new Function1<String, Object[]>() {
                public Object[] apply(String s) {
                  return new Object[] {s};
                }
              });
        }
      };
    }

}
