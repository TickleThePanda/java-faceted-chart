package uk.co.ticklethepanda.charting.faceted;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import uk.co.ticklethepanda.charting.faceted.internal.PointConverter;

/**
 * Contains the data required for plotting a graph.
 * 
 * Not thread safe. Won't act well if T is mutable
 * 
 * @author panda
 *
 * @param <T>
 *          The type of the items to be plotted.
 * @param <X>
 *          The type of the values being plotted on the x axis.
 * @param <Y>
 *          The type of the values being plotted on the y axis.
 * @param <G>
 *          The type of the categories.
 */
public class FacetedChartData<T, X, Y, G> {

  /**
   * A static factory method for creating a new FacetedChartData where the X
   * types are comparable and the Y types are comparable. It uses natural
   * ordering.
   * 
   * @param items
   *          The list of items to plot.
   * @param xAspect
   *          The function to get the X values from an item.
   * @param yAspect
   *          The function to get the Y values from an item.
   * @param groupAspect
   *          The function to get the group from the item.
   * @return a new FacetedChartData where the X types are comparable and the Y
   *         types are comparable.
   */
  public static <T, X extends Comparable<X>, Y extends Comparable<Y>, G> FacetedChartData<T, X, Y, G> withNaturalOrdering(
      List<T> items,
      Function<T, X> xAspect,
      Function<T, Y> yAspect,
      Function<T, G> groupAspect) {

    Comparator<X> xComparator = Comparator.naturalOrder();
    Comparator<Y> yComparator = Comparator.naturalOrder();

    return new FacetedChartData<>(
        items,
        xAspect,
        yAspect,
        groupAspect,
        xComparator,
        yComparator);
  }

  public static <T, X extends Comparable<X>, Y extends Comparable<Y>, G> FacetedChartData<T, X, Y, G> withSpecifiedOrdering(
      List<T> items,
      Function<T, X> xAspect,
      Function<T, Y> yAspect,
      Function<T, G> groupAspect,
      Comparator<X> xComparator,
      Comparator<Y> yComparator) {

    return new FacetedChartData<>(
        items,
        xAspect,
        yAspect,
        groupAspect,
        xComparator,
        yComparator);
  }

  private List<T> items;

  private Function<T, G> groupAspect;
  private Map<G, List<T>> groupMap;

  private Function<T, X> xAspect;
  private Function<T, Y> yAspect;

  private Comparator<X> xComparator;
  private Comparator<Y> yComparator;

  private FacetedChartData(List<T> items, Function<T, X> xAspect,
      Function<T, Y> yAspect, Function<T, G> groupAspect,
      Comparator<X> xComparator, Comparator<Y> yComparator) {
    groupMap = new HashMap<>();

    this.items = items;
    this.xAspect = xAspect;
    this.yAspect = yAspect;
    this.groupAspect = groupAspect;

    this.xComparator = xComparator;
    this.yComparator = yComparator;

    createMap();
  }

  public List<T> getItems(G group) {
    if (groupMap.get(group) != null) {
      return groupMap.get(group);
    } else {
      return new ArrayList<>();
    }
  }

  public List<G> getGroups() {
    List<G> groups = new ArrayList<>();
    for (G group : groupMap.keySet()) {
      groups.add(group);
    }
    return groups;
  }

  // TODO write unit tests
  public X getMaxX() {
    return getMax(xAspect, xComparator);
  }

  // TODO write unit tests
  public Y getMaxY() {
    return getMax(yAspect, yComparator);
  }

  // TODO write unit tests
  public X getMinX() {
    return getMin(xAspect, xComparator);
  }

  // TODO write unit tests
  public Y getMinY() {
    return getMin(yAspect, yComparator);
  }

  public List<X> getXValues(G group) {
    return getValuesForAxis(group, xAspect);
  }

  public List<Y> getYValues(G group) {
    return getValuesForAxis(group, yAspect);
  }

  public int size(G group) {
    return groupMap.get(group).size();
  }

  private void createMap() {
    for (T item : items) {
      G group = groupAspect.apply(item);
      addItemToGroup(item, group);
    }

    sortItemsByX();
  }

  private void addItemToGroup(T item, G group) {
    if (!groupMap.containsKey(group)) {
      groupMap.put(group, new ArrayList<T>());
    }

    groupMap.get(group).add(item);
  }

  private void sortItemsByX() {
    for (List<T> items : groupMap.values()) {
      items.sort((a, b) -> xComparator.compare(
          xAspect.apply(a),
          xAspect.apply(b)));
    }
  }

  private <E> E getMax(Function<T, E> aspect, Comparator<E> comparator) {
    return items.stream()
        .map(aspect)
        .max(comparator)
        .get();
  }

  private <E> E getMin(Function<T, E> aspect, Comparator<E> comparator) {
    return items.stream()
        .map(aspect)
        .min(comparator)
        .get();
  }

  private <E> List<E> getValuesForAxis(G group, Function<T, E> aspect) {
    if (groupMap.get(group) == null) {
      return new ArrayList<>();
    }

    List<E> values = new ArrayList<>();
    for (T item : groupMap.get(group)) {
      values.add(aspect.apply(item));
    }
    return values;
  }

  public List<Point2D> convertGroupToPoints(PointConverter<X, Y> converter, G group) {
    List<Point2D> points = new ArrayList<Point2D>();

    int numPoints = this.size(group);

    List<X> xValues = this.getXValues(group);
    List<Y> yValues = this.getYValues(group);

    for (int j = 0; j < numPoints; j++) {
      points.add(new Point2D.Double(
          converter.convertX(xValues.get(j)),
          converter.convertY(yValues.get(j))));
    }
    return points;
  }

  Rectangle2D getDataBounds(PointConverter<X, Y> converter) {

    double xMin = converter.convertX(this.getMinX());
    double xMax = converter.convertX(this.getMaxX());

    double yMin = converter.convertY(this.getMinY());
    double yMax = converter.convertY(this.getMaxY());

    double width = xMax - xMin;

    double height = yMax - yMin;

    return new Double(xMin, yMin, width, height);
  }
}
