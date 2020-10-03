package evohyp;

import domain.Booking;
import domain.Exam;
import domain.Period;
import domain.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class for applying Low-level heuristics of the Timetabling Problem.
 */
public class ConstructiveHeuristicEngine {
	public static String SUPPORTED_HEURISTICS = "ABCDEFGHI";

	private final ExamTimetablingProblem problem;
	private final Character character;

	public ConstructiveHeuristicEngine(ExamTimetablingProblem problem, Character heuristicChar) {
		this.problem = problem;
		this.character = heuristicChar;
	}

	/**
	 * Apply the heuristic to the given solution.
	 *
	 * @param solution - the solution to which to apply the heuristic.
	 * @return a new solution with the heuristic applied.
	 */
	public final ExamTimetablingSolution applyToSolution(ExamTimetablingSolution solution) {
		Exam exam;
		Period period;
		Room room;

		if (character == 'A') {
			exam = findExamWithMostClashes(solution);
			period = findPeriodWithLeastBookings(solution);
			room = findRandomRoom();
		} else if (character == 'B') {
			exam = findExamWithMostClashes(solution);
			period = findPeriodWithLeastOnSameDay(solution);
			room = findRandomRoom();
		} else if (character == 'C') {
			exam = findExamWithMostClashes(solution);
			period = findPeriodWithLeastStudents(solution);
			room = findRandomRoom();
		} else if (character == 'D') {
			exam = findExamWithMostStudents(solution);
			period = findPeriodWithLeastBookings(solution);
			room = findRandomRoom();
		} else if (character == 'E') {
			exam = findExamWithMostStudents(solution);
			period = findPeriodWithLeastOnSameDay(solution);
			room = findRandomRoom();
		} else if (character == 'F') {
			exam = findExamWithMostStudents(solution);
			period = findPeriodWithLeastStudents(solution);
			room = findRandomRoom();
		} else if (character == 'G') {
			exam = findExamWithMostWeightedClashes(solution);
			period = findPeriodWithLeastBookings(solution);
			room = findRandomRoom();
		} else if (character == 'H') {
			exam = findExamWithMostWeightedClashes(solution);
			period = findPeriodWithLeastOnSameDay(solution);
			room = findRandomRoom();
		} else if (character == 'I') {
			exam = findExamWithMostWeightedClashes(solution);
			period = findPeriodWithLeastStudents(solution);
			room = findRandomRoom();
		} else {
			throw new IllegalArgumentException(character + " is not a supported heuristic character.");
		}

		if (exam == null || period == null || room == null) return solution;
		List<Booking> bookings = new ArrayList<>(solution.bookings);
		Booking booking = new Booking(exam, period, room);
		bookings.add(booking);
		return new ExamTimetablingSolution(problem, bookings);
	}

	/**
	 * @param solution - the current solution for which to select the next exam.
	 * @return The examination with the most clashes (ie. shares students with the most other exams).
	 */
	public Exam findExamWithMostClashes(ExamTimetablingSolution solution) {
		List<Exam> unbookedExams = problem.exams.stream()
				.filter(e -> solution.bookings.stream().noneMatch(b -> b.exam.number == e.number))
				.collect(Collectors.toList());

		Exam selected = null;
		int selectedCount = Integer.MIN_VALUE;
		for (Exam exam : unbookedExams) {
			int examCount = (int) IntStream.of(problem.clashMatrix[exam.number]).filter(i -> i > 0).count();
			if (examCount > selectedCount) {
				selected = exam;
				selectedCount = examCount;
			}
		}

		return selected;
	}

	/**
	 * @param solution - the current solution for which to select the next exam.
	 * @return The examination with the most students enrolled.
	 */
	public Exam findExamWithMostStudents(ExamTimetablingSolution solution) {
		return problem.exams.stream()
				.filter(e -> solution.bookings.stream().noneMatch(b -> b.exam.number == e.number))
				.sorted((e1, e2) -> e2.students.size() - e1.students.size())
				.collect(Collectors.toList()).get(0);
	}

	/**
	 * @param solution - the current solution for which to select the next exam.
	 * @return The examination with the largest number of students involved in clashes is scheduled first.
	 */
	public Exam findExamWithMostWeightedClashes(ExamTimetablingSolution solution) {
		List<Exam> unbookedExams = problem.exams.stream()
				.filter(e -> solution.bookings.stream().noneMatch(b -> b.exam.number == e.number))
				.collect(Collectors.toList());

		Exam selected = null;
		int selectedCount = Integer.MIN_VALUE;
		for (Exam exam : unbookedExams) {
			int examCount = IntStream.of(problem.clashMatrix[exam.number]).sum();
			if (examCount > selectedCount) {
				selected = exam;
				selectedCount = examCount;
			}
		}

		return selected;
	}

	/**
	 * @param solution - the current solution for which to select the next period.
	 * @return The period with the least number of current bookings.
	 */
	public Period findPeriodWithLeastBookings(ExamTimetablingSolution solution) {
		Period selected = null;
		int selectedCount = Integer.MAX_VALUE;

		for (Period period : problem.periods) {
			int periodCount = (int) solution.bookings.stream().filter(b -> b.period.number == period.number).count();

			if (periodCount < selectedCount) {
				selected = period;
				selectedCount = periodCount;
			}
		}

		return selected;
	}

	/**
	 * @param solution - the current solution for which to select the next period.
	 * @return The period with the least number of current bookings on the same day.
	 */
	public Period findPeriodWithLeastOnSameDay(ExamTimetablingSolution solution) {
		Period selected = null;
		int selectedCount = Integer.MAX_VALUE;

		for (Period period : problem.periods) {
			int periodCount = (int) solution.bookings.stream().filter(b -> b.period.date.isEqual(period.date)).count();

			if (periodCount < selectedCount) {
				selected = period;
				selectedCount = periodCount;
			}
		}

		return selected;
	}

	/**
	 * @param solution - the current solution for which to select the next period.
	 * @return The period with the least number of current booked students.
	 */
	public Period findPeriodWithLeastStudents(ExamTimetablingSolution solution) {
		Period selected = null;
		int selectedCount = Integer.MAX_VALUE;

		for (Period period : problem.periods) {
			int periodCount = 0;
			List<Booking> periodBookings = solution.bookings.stream().filter(b -> b.period.number == period.number).collect(Collectors.toList());
			for (Booking booking : periodBookings) {
				periodCount += booking.exam.students.size();
			}

			if (periodCount < selectedCount) {
				selected = period;
				selectedCount = periodCount;
			}
		}

		return selected;
	}

	/**
	 * @return A random room.
	 */
	public Room findRandomRoom() {
		return problem.rooms.get(ThreadLocalRandom.current().nextInt(0, problem.rooms.size()));
	}
}
