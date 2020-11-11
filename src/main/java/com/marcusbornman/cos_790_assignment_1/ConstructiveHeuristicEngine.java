package com.marcusbornman.cos_790_assignment_1;

import dev.born.itc2007.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class for applying constructive Low-level heuristics to an examination timetabling problem to produce a solution
 * to the problem.
 */
public class ConstructiveHeuristicEngine {
	public static final String SUPPORTED_HEURISTICS = "ABCDEFGHI";
	public static Random random = new Random(0);

	private final ExamTimetablingProblem problem;

	/**
	 * @param problem - the problem for which this engine is going to produce solutions.
	 */
	public ConstructiveHeuristicEngine(ExamTimetablingProblem problem) {
		this.problem = problem;
	}

	/**
	 * @param heuristicChar - the character representative of the heuristic to apply.
	 * @param solution - the solution to which to apply the heuristic.
	 * @return a new solution with the given heuristic applied.
	 */
	public ExamTimetablingSolution applyToSolution(Character heuristicChar, ExamTimetablingSolution solution) {
		Exam exam;
		Period period;

		if (heuristicChar == 'A') {
			exam = findExamWithMostClashes(solution);
			period = findPeriodWithLeastBookings(solution);
		} else if (heuristicChar == 'B') {
			exam = findExamWithMostClashes(solution);
			period = findPeriodWithLeastOnSameDay(solution);
		} else if (heuristicChar == 'C') {
			exam = findExamWithMostClashes(solution);
			period = findPeriodWithLeastStudents(solution);
		} else if (heuristicChar == 'D') {
			exam = findExamWithMostStudents(solution);
			period = findPeriodWithLeastBookings(solution);
		} else if (heuristicChar == 'E') {
			exam = findExamWithMostStudents(solution);
			period = findPeriodWithLeastOnSameDay(solution);
		} else if (heuristicChar == 'F') {
			exam = findExamWithMostStudents(solution);
			period = findPeriodWithLeastStudents(solution);
		} else if (heuristicChar == 'G') {
			exam = findExamWithMostWeightedClashes(solution);
			period = findPeriodWithLeastBookings(solution);
		} else if (heuristicChar == 'H') {
			exam = findExamWithMostWeightedClashes(solution);
			period = findPeriodWithLeastOnSameDay(solution);
		} else if (heuristicChar == 'I') {
			exam = findExamWithMostWeightedClashes(solution);
			period = findPeriodWithLeastStudents(solution);
		} else {
			throw new IllegalArgumentException(heuristicChar + " is not a supported heuristic character.");
		}
		Room room = randomRoom();

		return applyToSolution(solution, exam, period, room);
	}

	/**
	 * @param solution the solution to which to add a new booking.
	 * @param exam     the exam for the new booking.
	 * @param period   the period for the new booking.
	 * @param room     the room for the new booking.
	 * @return the new solution with the new booking added.
	 */
	private ExamTimetablingSolution applyToSolution(ExamTimetablingSolution solution, Exam exam, Period period, Room room) {
		Booking booking = new Booking(exam, period, room);
		List<Booking> newBookings = new ArrayList<>(solution.bookings);
		newBookings.add(booking);
		return new ExamTimetablingSolution(problem, newBookings);
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
		List<Exam> unbookedExams = problem.exams.stream()
				.filter(e -> solution.bookings.stream().noneMatch(b -> b.exam.number == e.number))
				.collect(Collectors.toList());

		Exam selected = null;
		int selectedCount = Integer.MIN_VALUE;
		for (Exam exam : unbookedExams) {
			int examCount = exam.students.size();
			if (exam.students.size() > selectedCount) {
				selected = exam;
				selectedCount = examCount;
			}
		}

		return selected;
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
	public Room randomRoom() {
		return problem.rooms.get(random.nextInt(problem.rooms.size()));
	}
}
