package dev.ade.project.orm;

import dev.ade.project.util.ConnectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdeOrm {
    private Connection conn;

    public AdeOrm() {
    }

    public AdeOrm(Connection connection) {
        conn = connection;
    }

    /**
     * Get a String column value of a record by a String primary key
     */
    public String getStringColumn(String tableName, String columnName, String id, String idValue) {
        String sql = "select " + columnName + " from " + tableName + " where " + id + "=?";
        String result = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idValue);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString(columnName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get a generic type column value of a record by a primary key of any type
     *
     * @param tableName table to be read
     * @param columnName column to be retrieve
     * @param id column name of the primary key
     * @param idValue primary key value of a record to be retrieve
     * @return
     */
    public <T> T getColumn(String tableName, String columnName, String id, Object idValue) {
        String sql = "select " + columnName + " from " + tableName + " where " + id + "=?";
        T result = null;
        String idValueType = idValue.getClass().getSimpleName();
        String setObject = "set" + idValueType;
        Method method;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Class<?> clazz = PreparedStatement.class;
            method = clazz.getDeclaredMethod(setObject, int.class, idValue.getClass());
            Object[] psParams = new Object[]{1, idValue};
            method.invoke(ps, psParams);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = (T)rs.getString(columnName);
            }
        } catch (SQLException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get generic type columns' values of a record by a primary key of any type.
     *
     * @param tableName table to be read
     * @param columnNames a list of column names of the table to retrieve
     * @param id column name of the primary key
     * @param idValue primary key value of a record to be retrieve
     * @return
     */
    public List<Object>getRecord(String tableName, List<String> columnNames, String id, Object idValue) {
        String colNames = String.join(", ", columnNames);
        String sql = "select " + colNames + " from " + tableName + " where " + id + "=?";
        List<Object> result = new ArrayList<>();
        String idValueType = idValue.getClass().getSimpleName();
        String setObject = "set" + idValueType;
        Method method;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Class<?> clazz = PreparedStatement.class;
            method = clazz.getDeclaredMethod(setObject, int.class, idValue.getClass());
            Object[] psParams = new Object[]{1, idValue};
            method.invoke(ps, psParams);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                for (int i = 0; i < columnNames.size(); i++) {
                    result.add(rs.getString(columnNames.get(i)));
                }
            }
        } catch (SQLException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get generic type columns' values of record(s) by a column value of any type.
     * If the primary key is used, only return one record, if other non unique value
     * column is used, return all records with the column value
     *
     * @param tableName table to be read
     * @param columnNames a list of column names of the table to retrieve
     * @param id a column name
     * @param idValue the column value of record(s) to be retrieve
     * @return
     */
    public List<List<Object>>getRecords(String tableName, List<String> columnNames, String id, Object idValue) {
        String colNames = String.join(", ", columnNames);
        String sql = "select " + colNames + " from " + tableName + " where " + id + "=?";
        List<List<Object>> result = new ArrayList<>();
        String idValueType = idValue.getClass().getSimpleName();
        String setObject = "set" + idValueType;
        Method method;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Class<?> clazz = PreparedStatement.class;
            method = clazz.getDeclaredMethod(setObject, int.class, idValue.getClass());
            Object[] psParams = new Object[]{1, idValue};
            method.invoke(ps, psParams);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<Object> record = new ArrayList<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    record.add(rs.getString(columnNames.get(i)));
                }
                result.add(record);
            }
        } catch (SQLException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get generic type columns' values of all records in a table
     *
     * @param tableName the name of a table
     * @return
     */
    public List<List<Object>>getRecords(String tableName, List<String> columnNames) {
        String colNames = String.join(", ", columnNames);
        String sql = "select " + colNames + " from " + tableName;
        List<List<Object>> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<Object> record = new ArrayList<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    record.add(rs.getString(columnNames.get(i)));
                }
                result.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get generic type values of record(s) by a list of conditions (key, value) pairs under
     * "and" or "or" relationship.
      *
     * @param tableName table to be read
     * @param columnNames a list of column names of the table to retrieve
     * @param conditions a list of Condition objects with a key and a value field
     * @param criteria "and" or "or" to specific the conditions criteria
     * @return
     */
    public List<List<Object>>getRecordsWithConditions(String tableName, List<String> columnNames,
                                                      List<Condition> conditions, String criteria) {
        String colNames = String.join(", ", columnNames);
        String sql = "select " + colNames + " from " + tableName + " where ";

        if (criteria.equals("and")) {
            String s = conditions.stream().map(Condition::getKey).collect(Collectors.joining("=? and "));
            sql += s + "=?";
        }
        if (criteria.equals("or")) {
            String s = conditions.stream().map(Condition::getKey).collect(Collectors.joining("=? or "));
            sql += s + "=?";
        }

        List<List<Object>> result = new ArrayList<>();
        Method method = null;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Class<?> clazz = PreparedStatement.class;
            for (int i = 0; i < conditions.size(); i++) {
                Object value = conditions.get(i).getValue();
                String valueType = value.getClass().getSimpleName();
                String setObject = "set" + valueType;
                method = clazz.getDeclaredMethod(setObject, int.class, value.getClass());
                Object[] psParams = new Object[]{i+1, value};
                method.invoke(ps, psParams);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<Object> record = new ArrayList<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    record.add(rs.getString(columnNames.get(i)));
                }
                result.add(record);
            }
        } catch (SQLException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }
}
