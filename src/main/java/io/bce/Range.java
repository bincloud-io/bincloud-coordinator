package io.bce;

import java.util.Comparator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class Range<T> {
	private final Point min;
	private final Point max;
	private final Comparator<T> comparator;

	protected Range(T leftPoint, T rightPoint, Comparator<T> valuesComparator) {
		super();
		this.min = new Point(leftPoint);
		this.max = new Point(rightPoint);
		this.comparator = valuesComparator;
		checkThatMinAmountIsntGreaterThenMaxAmount();
	}

	/**
	 * Create the range for case when the values comparison should be performed by
	 * the comparator
	 * 
	 * @param <T>        The value type name
	 * @param min        The minimal value
	 * @param max        The maximal value
	 * @param comparator The values comparator
	 * @return The range value
	 */
	public static final <T> Range<T> createFor(T min, T max, Comparator<T> comparator) {
		return new Range<T>(min, max, comparator);
	}

	public static final <T extends Comparable<T>> Range<T> createFor(T min, T max) {
		return createFor(min, max, (left, right) -> left.compareTo(right));
	}

	public T getMin() {
		return min.extract();
	}

	public T getMax() {
		return max.extract();
	}
	
	public boolean contains(T value) {
		Point point = new Point(value);
		return (point.compareTo(min) >= 0) && (point.compareTo(max) <= 0); 
	}

	private final void checkThatMinAmountIsntGreaterThenMaxAmount() {
		if (min.compareTo(max) > 0) {
			throw new ThresholdsAmountsException(getMin(), getMax());
		}
	}

	@RequiredArgsConstructor
	private class Point implements Comparable<Point>, Wrapped<T> {
		private final T value;

		@Override
		public int compareTo(Point opposite) {
			return comparator.compare(value, opposite.value);
		}

		@Override
		public T extract() {
			return value;
		}
	}

	public static final class ThresholdsAmountsException extends RuntimeException {
		private static final long serialVersionUID = 3344037211261126041L;

		public ThresholdsAmountsException(Object min, Object max) {
			super(String.format("Range format error. Min value amount [%s] must not be greater then max [%s]", min,
					max));
		}
	}
}
