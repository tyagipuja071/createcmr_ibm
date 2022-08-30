/**
 * 
 */
package com.ibm.cio.cmr.request.util.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 136786PH1
 *
 */
public class ReviewGroup implements Comparable<ReviewGroup> {

  private String name;
  private int priority;
  private ReviewCategory currentMatch;
  private List<ReviewCategory> categories = new ArrayList<>();

  public ReviewGroup(String name) {
    this.name = name;
  }

  public void add(ReviewCategory category) {
    this.categories.add(category);
  }

  public ReviewCategory match(String reqType, String comment, String approvalStatus) {
    this.currentMatch = null;
    for (ReviewCategory category : this.categories) {
      if (category.matches(reqType, comment, approvalStatus)) {
        this.currentMatch = category;
        return category;
      }
    }
    return null;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getName() {
    return name;
  }

  @Override
  public int compareTo(ReviewGroup o) {
    return this.priority < o.priority ? -1 : (this.priority > o.priority ? 1 : 0);
  }

  public ReviewCategory getCurrentMatch() {
    return currentMatch;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (ReviewCategory cat : this.categories) {
      sb.append(cat.display(this));
    }
    return sb.toString();

  }

}
