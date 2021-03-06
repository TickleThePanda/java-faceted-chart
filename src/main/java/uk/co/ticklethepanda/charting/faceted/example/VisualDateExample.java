package uk.co.ticklethepanda.charting.faceted.example;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import uk.co.ticklethepanda.charting.faceted.Chart;
import uk.co.ticklethepanda.charting.faceted.FacetedChartData;
import uk.co.ticklethepanda.charting.faceted.FacetedChartFactory;

public class VisualDateExample {

  private static class Item {
    private int valueX;

    @Override
    public String toString() {
      return "Item [valueX=" + valueX + ", valueY=" + valueY + ", group=" + group + "]";
    }

    private LocalTime valueY;

    private int group;

    public Item(int valueX, LocalTime valueY, int group) {
      this.valueX = valueX;
      this.valueY = valueY;
      this.group = group;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Item other = (Item) obj;
      if (group != other.group)
        return false;
      if (valueX != other.valueX)
        return false;
      if (valueY != other.valueY)
        return false;
      return true;
    }

    public Integer getGroup() {
      return group;
    }

    public Integer getValueX() {
      return valueX;
    }

    public LocalTime getValueY() {
      return valueY;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + group;
      result = prime * result + valueX;
      result = prime * result + valueY.get(ChronoField.SECOND_OF_DAY);
      return result;
    }
  }

  private static Random random = new Random();

  public static void main(String[] args) throws IOException {
    List<Item> items = createItems();

    FacetedChartData<Item, LocalTime, Integer, Integer> data =
        FacetedChartData.withNaturalOrdering(
            items,
            Item::getValueY,
            Item::getValueX,
            Item::getGroup);

    Chart painter = FacetedChartFactory.createNumberAgainstLocalTime(data);

    RenderedImage image = painter.draw();

    File outputFile = getOutputFile();

    ImageIO.write(image, "png", outputFile);
  }

  private static File getOutputFile() {
    new File("output").mkdirs();

    File outputFile = new File("output/image-" + VisualDateExample.class.getCanonicalName() + ".png");
    return outputFile;
  }

  private static List<Item> createItems() {
    List<Item> items = new ArrayList<>();

    for (int i = 0; i < 1000; i++) {
      items.add(createRandomItem());
    }
    return items;
  }

  private static Item createRandomItem() {
    return new Item(
        random.nextInt(100) - 13,
        LocalTime.ofSecondOfDay(random.nextInt(100)),
        random.nextInt(10));
  }

}
