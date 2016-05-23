package com.duitang.saturn.client;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一些共性内容: 1. 重写toString() 1. toString(), 完美适用于该类的子类，其他适配地一般般。 若将来需要更好支持请查看
 * {@link org.apache.commons.lang3.builder.ReflectionToStringBuilder}
 * 
 * @author Jacob.Zhang
 * @since 2015年10月23日 下午2:53:50
 */
public class ServiceBasePOJO implements Serializable {

  private static final long serialVersionUID = 2172566287625899620L;

  private static final int FILED_LENGTH_LIMITED = 300;
  
  private List<String> dtInvisableFiledNames;

  private List<String> dtVisableFiledNames;

  public ServiceBasePOJO() {}

  @Override
  public String toString() {
    try {
      return toString(this);
    } catch (Exception e) {
      return super.toString();
    }
  }

  /**
   * It works well with the ServiceBasePOJO's child class. The POJO that has complex nested in class
   * like collections(List, Map) is not being supported well.
   * 
   * @param value any Object
   * @return String
   * @throws Exception ex
   */
  private String toString(Object value) throws Exception {
    StringBuilder result = new StringBuilder();
    String newLine = System.getProperty("line.separator"); // line separator ("\n" on the OS)

    result.append(value.getClass().getName());
    result.append(" {");
    result.append(newLine);

    // determine fields declared in this class(including fields of superclass)
    Field[] fields = value.getClass().getDeclaredFields();

    Map<String, Field> filedMap = new HashMap<String, Field>();
    for (Field field : fields) {
      filedMap.put(field.getName(), field);
    }

    if (null != dtVisableFiledNames && !dtVisableFiledNames.isEmpty()) {
      fields = new Field[dtVisableFiledNames.size()];
      for (int i = 0; i < fields.length; i++) {
        fields[i] = filedMap.get(dtVisableFiledNames.get(i));
      }
    }

    if (null == dtVisableFiledNames && null != dtInvisableFiledNames
        && !dtInvisableFiledNames.isEmpty()) {
      for (String name : dtInvisableFiledNames) {
        filedMap.remove(name);
      }
      fields = filedMap.values().toArray(new Field[fields.length - dtInvisableFiledNames.size()]);
    }

    for (Field field : fields) {
      // set all accessible
      field.setAccessible(true);
      // ignore class in package java.lang and java.util, most of them have override toString()
      if (null != field.get(value) && !field.get(value).getClass().isEnum()
          && !field.get(value).getClass().getName().contains("java.lang")
          && !field.get(value).getClass().getName().contains("java.util")) {
        result.append(toString(field.get(value)));
      } else {
        result.append("  ");
        try {
          result.append(field.getName());
          result.append(": ");
          // cut off long filed
          if (field.get(value) instanceof String
              && field.get(value).toString().length() >= FILED_LENGTH_LIMITED) {
            result.append(field.get(value).toString().substring(0, FILED_LENGTH_LIMITED));
            result.append("...");
          } else {
            result.append(field.get(value));
          }
        } catch (IllegalAccessException ex) {
          return super.toString();
        }
        result.append(newLine);
      }
    }
    result.append("}");

    return result.toString();
  }

  protected void fillDtInvisableFiledNames(String... filedNames) {
    this.dtInvisableFiledNames = new ArrayList<String>();
    dtInvisableFiledNames.add("serialVersionUID");
    for (String filedName : filedNames) {
      dtInvisableFiledNames.add(filedName);
    }
  }

  protected void fillDtVisableFiledNames(String... filedNames) {
    this.dtVisableFiledNames = new ArrayList<String>();
    for (String filedName : filedNames) {
      dtVisableFiledNames.add(filedName);
    }
  }

}
