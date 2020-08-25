package timetabling;

import genalg.GenAlg;
import initialsoln.InitialSoln;
import timetabling.evohyp.HeuristicApplier;
import timetabling.utils.ProblemReader;

import java.io.IOException;
import java.io.PrintWriter;

public class Main {
	public static void main(String[] args) {
		try {
			long seed = System.currentTimeMillis();
			String heuristics = HeuristicApplier.SUPPORTED_HEURISTICS;

			GenAlg genAlg = new GenAlg(seed, heuristics);
			genAlg.setPopulationSize(50);
			genAlg.setTournamentSize(10);
			genAlg.setNoOfGenerations(1);
			genAlg.setMutationRate(0.05);
			genAlg.setCrossoverRate(0.75);
			genAlg.setInitialMaxLength(10);
			genAlg.setOffspringMaxLength(50);
			genAlg.setMutationLength(5);

			runTests(genAlg, "problems/exam_comp_set1.exam");
			runTests(genAlg, "problems/exam_comp_set2.exam");
			runTests(genAlg, "problems/exam_comp_set3.exam");
			runTests(genAlg, "problems/exam_comp_set4.exam");
			runTests(genAlg, "problems/exam_comp_set5.exam");
			runTests(genAlg, "problems/exam_comp_set6.exam");
			runTests(genAlg, "problems/exam_comp_set7.exam");
			runTests(genAlg, "problems/exam_comp_set8.exam");
			runTests(genAlg, "problems/exam_comp_set9.exam");
			runTests(genAlg, "problems/exam_comp_set10.exam");
			runTests(genAlg, "problems/exam_comp_set11.exam");
			runTests(genAlg, "problems/exam_comp_set12.exam");
		} catch (IOException e) {
			System.out.println("File at path could not be found.");
		}
	}

	private static void runTests(GenAlg genAlg, String filePath) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 1; i++) {
			genAlg.setProblem(ProblemReader.problemFromFile(filePath));
			InitialSoln solution = genAlg.evolve();

			stringBuilder.append("BEST SOLUTION FOR TEST RUN ").append(i).append("\n========================================\n");
			stringBuilder.append("Fitness: ").append(solution.getFitness()).append("\n");
			stringBuilder.append("Heuristic combination: ").append(solution.getHeuCom()).append("\n");
			stringBuilder.append("Solution: ").append(solution.solnToString()).append("\n\n\n");
		}

		try (PrintWriter out = new PrintWriter("solutions/" + filePath.substring(filePath.lastIndexOf("/") + 1))) {
			out.println(stringBuilder.toString());
		}
	}
}
