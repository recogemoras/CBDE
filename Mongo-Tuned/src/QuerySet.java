import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class QuerySet {

	public void executeQueries(DB database)  {
		System.out.println("-------- Queries ------");

		Long average = (long) 0;
		List<Long> timeDifferences = new ArrayList<Long>();
		for (int j = 0; j < 5; ++j) {
			timeDifferences.add(j, query1(database));
		}

		System.out.println("Query 1 took " + timeDifferences + 
				" in nanoseconds --- with minimum " + Collections.min(timeDifferences));
		average += Collections.min(timeDifferences);

		timeDifferences = new ArrayList<Long>();
		for (int j = 0; j < 5; ++j) {
			timeDifferences.add(j, query2(database));
		}

		System.out.println("Query 2 took " + timeDifferences + 
				" in nanoseconds --- with minimum " + Collections.min(timeDifferences));
		average += Collections.min(timeDifferences);

		timeDifferences = new ArrayList<Long>();
		for (int j = 0; j < 5; ++j) {
			timeDifferences.add(j, query3(database));
		}

		System.out.println("Query 3 took " + timeDifferences + 
				" in nanoseconds --- with minimum " + Collections.min(timeDifferences));
		average += Collections.min(timeDifferences);

		timeDifferences = new ArrayList<Long>();
		for (int j = 0; j < 5; ++j) {
			timeDifferences.add(j, query4(database));
		}

		System.out.println("Query 4 took " + timeDifferences + 
				" in nanoseconds --- with minimum " + Collections.min(timeDifferences));
		average += Collections.min(timeDifferences);


		System.out.println("\nAverage query time " + average/4 + " in nanoseconds\n");

	}

	private Long query1(DB database) {
		Long start = System.nanoTime();

		//els mesos comencen en 0
		Calendar calendar = new GregorianCalendar(2013,03,30);

		BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("L_ShipDate", new BasicDBObject("$lte", calendar.getTime())));

		BasicDBObject fields = new BasicDBObject("L_ReturnFlag", 1);
		fields.put("L_LineStatus", 1);
		fields.put("L_ShipDate", 1);
		fields.put("L_Quantity", 1);
		fields.put("L_ExtendedPrice", 1);
		fields.put("L_ReturnFlag", 1);
		fields.put("L_Tax", 1);
		fields.put("L_Discount", 1);
		fields.put("_id", 0);
		BasicDBObject project = new BasicDBObject("$project", fields );

		BasicDBObject sortFields = new BasicDBObject( "L_ReturnFlag", 1 );
		sortFields.put("L_LineStatus", 1 );

		BasicDBObject sort = new BasicDBObject( "$sort", sortFields);

		DBCollection myColl = database.getCollection("lineitem");
		AggregationOutput out = myColl.aggregate(match, project, sort);

		Map<String, BasicDBObject> result = new LinkedHashMap<String, BasicDBObject>();
		for (DBObject object : out.results()) {
			String key = object.get("L_ReturnFlag") + " - " + object.get("L_LineStatus");
			BasicDBObject group = result.get(key);
			if (group == null) {
				group = new BasicDBObject();
				group.put("L_ReturnFlag", object.get("L_ReturnFlag"));
				group.put("L_LineStatus", object.get("L_LineStatus"));
				group.put("sum_qty", 0);
				group.put("sum_base_price", 0);
				group.put("sum_disc_price", 0);
				group.put("sum_charge", 0);
				group.put("count_order", 0);
				group.put("sum_discount", 0);
			}
			int quantity = new Integer(object.get("L_Quantity").toString());
			int sumQuantity = group.getInt("sum_qty") + quantity;
			group.put("sum_qty", sumQuantity);

			double discount = new Double(object.get("L_Discount").toString());

			double extendedPrice = new Double(object.get("L_ExtendedPrice").toString());
			double sumExtended = group.getDouble("sum_base_price") + extendedPrice;
			group.put("sum_base_price", sumExtended);

			double sum_disc_price = group.getDouble("sum_disc_price") + (extendedPrice * (1-discount));
			group.put("sum_disc_price", sum_disc_price);

			double tax = new Double(object.get("L_Tax").toString());
			double sum_charge = group.getDouble("sum_charge") + (extendedPrice * (1-discount) * (1 + tax));
			group.put("sum_charge", sum_charge);

			int num_elements = group.getInt("count_order") + 1;
			group.put("count_order", num_elements);

			double sum_discount = group.getDouble("sum_discount") + discount;
			group.put("sum_discount", sum_discount);

			result.put(key, group);
		}

		for (String key : result.keySet()) {
			BasicDBObject group = result.get(key);
			double average_quantity = group.getDouble("sum_qty") / group.getDouble("count_order");
			group.put("avg_qty", average_quantity);

			double average_extended = group.getDouble("sum_base_price") / group.getDouble("count_order");
			group.put("avg_price", average_extended);

			double average_discount = group.getDouble("sum_discount") / group.getDouble("count_order");
			group.put("avg_disc", average_discount);

			group.remove("sum_discount");
		}
		Long end = System.nanoTime();


		DBCollection resultTable = database.getCollection( "resultQuery1" );
		BasicDBObject resultObject = new BasicDBObject();
		resultObject.put("numResults", result.size());
		List<BasicDBObject> firstResults = new ArrayList<BasicDBObject>();
		int count = 0;
		for (String key : result.keySet()) {
			firstResults.add(result.get(key));
			++ count;
			if (count == 20) break;
		}
		resultObject.put("result", firstResults);
		resultTable.insert(resultObject);

		return end-start;
	}

	@SuppressWarnings("unchecked")
	private Long query2(DB database) {
		Long start = System.nanoTime();

		// Part
		Pattern regex = Pattern.compile( "12345678901234567890123456789012" );
		BasicDBObject clause1 = new BasicDBObject( "P_Type", regex );   
		BasicDBObject clause2 = new BasicDBObject( "P_Size", 1000 );
		BasicDBList and = new BasicDBList();
		and.add( clause1 );
		and.add( clause2 );
		DBObject query = new BasicDBObject( "$and", and );
		BasicDBObject match = new BasicDBObject( "$match", query );

		BasicDBObject fields = new BasicDBObject();
		fields.put( "_id", 1 );
		fields.put( "P_Mfgr", 1 );
		BasicDBObject project = new BasicDBObject( "$project", fields );

		BasicDBObject sortFields = new BasicDBObject( "_id", 1 );
		BasicDBObject sort = new BasicDBObject( "$sort", sortFields );

		DBCollection partColl = database.getCollection( "part" );
		AggregationOutput partOut = partColl.aggregate( match, project, sort );

		// Region
		match = new BasicDBObject( "$match", new BasicDBObject( "R_Name", "12345678901234567890123456789012" ) );

		fields = new BasicDBObject( "R_Name", 1 );
		fields.put("nations", 1);
		fields.put( "_id", 1 );
		project = new BasicDBObject( "$project", fields );

		DBCollection regionColl = database.getCollection( "region" );
		AggregationOutput regionOut = regionColl.aggregate( match, project );

		List<BasicDBObject> result = new ArrayList<BasicDBObject>();
		for (DBObject region : regionOut.results()) {
			List<BasicDBObject> nations = (List<BasicDBObject>) region.get("nations");
			for (DBObject nation : nations) {
				List<BasicDBObject> suppliers = (List<BasicDBObject>) nation.get("suppliers");
				for (DBObject supplier : suppliers) {
					List<BasicDBObject> partsupps = (List<BasicDBObject>) supplier.get("partsupps");
					for (DBObject partsupp : partsupps) {
						for (DBObject part : partOut.results()) {
							Integer partKey = (Integer) part.get("_id");
							if (partsupp.get("PS_PartKey").equals(partKey) && partsupp.get("PS_SupplyCost").equals(getSubquery2(database, partKey))) {
								BasicDBObject rowResult = new BasicDBObject();

								rowResult.put( "S_AcctBal", supplier.get( "S_AcctBal" ) );
								rowResult.put( "S_Name", 	supplier.get( "S_Name" ) );
								rowResult.put( "N_Name", 	nation.get( "N_Name" ) );
								rowResult.put( "P_PartKey", part.get( "_id" ) );
								rowResult.put( "P_Mfgr", 	part.get( "P_Mfgr" ) );
								rowResult.put( "S_Address", supplier.get( "S_Address" ) );
								rowResult.put( "S_Phone", 	supplier.get( "S_Phone" ) );
								rowResult.put( "S_Comment", supplier.get( "S_Comment" ) );

								result.add(rowResult);
							}
						}
					}
				}
			}
		}

		Collections.sort(result, new Comparator<BasicDBObject>() {

			@Override
			public int compare(BasicDBObject arg0, BasicDBObject arg1) {
				Double acctbal1 = arg0.getDouble("S_AcctBal");
				Double acctbal2 = arg1.getDouble("S_AcctBal");
				if (acctbal2.compareTo(acctbal1) != 0) {
					return acctbal2.compareTo(acctbal1);
				}
				String name1 = arg0.getString("N_Name");
				String name2 = arg1.getString("N_Name");
				if (name1.compareTo(name2) != 0) {
					return name1.compareTo(name2);
				}
				name1 = arg0.getString("S_Name");
				name2 = arg1.getString("S_Name");
				if (name1.compareTo(name2) != 0) {
					return name1.compareTo(name2);
				}
				String partkey1 = arg0.getString("P_PartKey");
				String partkey2 = arg1.getString("P_PartKey");
				return partkey1.compareTo(partkey2);
			}

		});

		Long end = System.nanoTime();

		DBCollection resultTable = database.getCollection( "resultQuery2" );
		BasicDBObject resultObject = new BasicDBObject();
		resultObject.put("numResults", result.size());
		resultObject.put("result", result);
		resultTable.insert(resultObject);

		return end-start;
	}

	@SuppressWarnings("unchecked")
	private double getSubquery2(DB database, int partKey) {
		// Region
		BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("R_Name", "12345678901234567890123456789012"));

		BasicDBObject fields = new BasicDBObject("R_Name", 1);
		fields.put("nations", 1);
		fields.put("_id", 1);
		BasicDBObject project = new BasicDBObject("$project", fields );

		DBCollection regionColl = database.getCollection("region");
		AggregationOutput regionOut = regionColl.aggregate(match, project);

		double minimum = Double.MAX_VALUE;
		for (DBObject region : regionOut.results()) {
			List<BasicDBObject> nations = (List<BasicDBObject>) region.get("nations");
			for (DBObject nation : nations) {
				List<BasicDBObject> suppliers = (List<BasicDBObject>) nation.get("suppliers");
				for (DBObject supplier : suppliers) {
					List<BasicDBObject> partsupps = (List<BasicDBObject>) supplier.get("partsupps");
					for (DBObject partsupp : partsupps) {
						if (partsupp.get("PS_PartKey").equals(partKey)) {
							minimum = Math.min(minimum, new Double(partsupp.get("PS_SupplyCost").toString()));
						}
					}
				}
			}
		}

		return minimum;
	}

	@SuppressWarnings("unchecked")
	private Long query3(DB database) {
		Long start = System.nanoTime();

		BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("C_MktSegment", "12345678901234567890123456789012"));

		BasicDBObject fields = new BasicDBObject("orders", 1);
		fields.put("_id", 0);
		BasicDBObject project = new BasicDBObject("$project", fields );

		// Customer
		DBCollection myColl = database.getCollection("customer");
		AggregationOutput out = myColl.aggregate(match, project);

		Calendar calendar = new GregorianCalendar(2013,3,29);
		Date o_refDate = calendar.getTime();
		Calendar calendar2 = new GregorianCalendar(2013,3,30);
		Date l_refDate = calendar2.getTime();

		List<DBObject> result = new ArrayList<DBObject>();
		for (DBObject customer : out.results()) {
			List<DBObject> orders = (List<DBObject>) customer.get("orders");
			for (DBObject order : orders) {
				Date o_orderDate = (Date) order.get("O_OrderDate");
				if (o_orderDate.before(o_refDate)) {
					List<DBObject> lineitems = (List<DBObject>) order.get("lineitems");
					for (DBObject lineitem : lineitems) {
						Date l_shipDate = (Date) lineitem.get("L_ShipDate");
						if (l_shipDate.after(l_refDate)) {
							BasicDBObject object = new BasicDBObject();
							object.put("l_orderkey", order.get("O_OrderKey"));
							double extendedPrice = (Double) lineitem.get("L_ExtendedPrice");
							double discount = (Double) lineitem.get("L_Discount");
							double revenue = extendedPrice * (1 - discount);
							object.put("revenue", revenue);
							object.put("o_orderdate", o_orderDate);
							object.put("o_shippriority", order.get("O_ShipPriority"));
							result.add(object);
						}
					}
				}
			}
		}

		Map<String, List<DBObject>> groups = new HashMap<String, List<DBObject>>();
		for (DBObject object : result) {
			String key = object.get("l_orderkey").toString() + "-" + object.get("o_orderdate").toString() + "-" + object.get("o_shippriority").toString();
			List<DBObject> list = groups.get(key);
			if (list == null) {
				list = new ArrayList<DBObject>();
				groups.put(key, list);
			}
			list.add(object);
		}

		List<DBObject> finalResult = new ArrayList<DBObject>();
		for (String key : groups.keySet()) {
			BasicDBObject row = new BasicDBObject();
			double total = 0;
			for (DBObject obj : groups.get(key)) total += (Double) obj.get("revenue");
			row.put("revenue", total);
			DBObject first = groups.get(key).get(0);
			row.put("l_orderkey", first.get("l_orderkey"));
			row.put("o_orderdate", first.get("o_orderdate"));
			row.put("o_shippriority", first.get("o_shippriority"));
			finalResult.add(row);
		}

		Collections.sort(finalResult, new Comparator<DBObject>() {

			@Override
			public int compare(DBObject o1, DBObject o2) {
				Double revenue1 = (Double) o1.get("revenue");
				Double revenue2 = (Double) o2.get("revenue");
				if (!revenue1.equals(revenue2)) {
					return revenue2.compareTo(revenue1);
				}
				Date orderDate1 = (Date) o1.get("o_orderdate");
				Date orderDate2 = (Date) o2.get("o_orderdate");
				return orderDate1.compareTo(orderDate2);
			}

		});

		Long end = System.nanoTime();

		DBCollection resultTable = database.getCollection( "resultQuery3" );
		BasicDBObject resultObject = new BasicDBObject();
		int count = 0;
		List<DBObject> firstresults = new ArrayList<DBObject>();
		for (DBObject obj : finalResult) {
			++ count;
			if (count <= 20) firstresults.add(obj);
		}
		resultObject.put("numResults", count);

		resultObject.put("result", firstresults);
		resultTable.insert(resultObject);

		return end-start;
	}

	@SuppressWarnings("unchecked")
	private Long query4(DB database) {
		Long start = System.nanoTime();


		// Customer + Orders + Lineitem
		BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("C_MktSegment", "12345678901234567890123456789012"));

		BasicDBObject fields = new BasicDBObject("orders", 1);
		fields.put("_id", 0);
		fields.put("C_NationKey", 1);
		BasicDBObject project = new BasicDBObject("$project", fields );

		DBCollection customerColl = database.getCollection("customer");
		AggregationOutput customerOut = customerColl.aggregate(match, project);

		// Region + Nation + Supplier
		// Region
		match = new BasicDBObject( "$match", new BasicDBObject( "R_Name", "12345678901234567890123456789012" ) );

		fields = new BasicDBObject("nations", 1);
		fields.put( "_id", 1 );
		project = new BasicDBObject( "$project", fields );
		
		DBCollection regionColl = database.getCollection("region");
		AggregationOutput regionOut = regionColl.aggregate(match, project);
		
		List<BasicDBObject> suppliernations = new ArrayList<BasicDBObject>();
		for (DBObject region : regionOut.results()) {
			List<DBObject> nations = (List<DBObject>) region.get("nations");
			for (DBObject nation : nations) {
				List<DBObject> suppliers = (List<DBObject>) nation.get("suppliers");
				for (DBObject supplier : suppliers) {
					BasicDBObject row = new BasicDBObject("S_NationKey", nation.get("N_NationKey"));
					row.put("S_SuppKey", supplier.get("S_SuppKey"));
					row.put("N_Name", nation.get("N_Name"));
					suppliernations.add(row);
				}
			}
		}
		
		Calendar calendar = new GregorianCalendar(2013,3,29);
		Date refDate1 = calendar.getTime();
		Calendar calendar2 = new GregorianCalendar(2014,3,30);
		Date refDate2 = calendar2.getTime();
		
		Map<String, Double> groups = new HashMap<String, Double>();
		for (DBObject customer : customerOut.results()) {
			List<DBObject> suppliers = new ArrayList<DBObject>();
			for (DBObject supp : suppliernations) {
				if (supp.get("S_NationKey").equals(customer.get("C_NationKey"))) {
					suppliers.add(supp);
				}
			}
			if (!suppliers.isEmpty()) {
				List<DBObject> orders = (List<DBObject>) customer.get("orders");
				for (DBObject order : orders) {
					Date o_orderdate = (Date) order.get("O_OrderDate");
					if (o_orderdate.before(refDate2) && o_orderdate.after(refDate1)) {
						List<DBObject> lineitems = (List<DBObject>) order.get("lineitems");
						for (DBObject lineitem : lineitems) {
							for (DBObject supplier : suppliers) {
								if (supplier.get("S_SuppKey").equals(lineitem.get("L_SuppKey"))) {
									String key = supplier.get("N_Name").toString();
									
									Double extendedPrice = (Double) lineitem.get("L_ExtendedPrice");
									Double discount = (Double) lineitem.get("L_Discount");
									Double revenue = extendedPrice * (1-discount);
									
									Double groupRevenue = groups.get(key);
									if (groupRevenue == null) {
										groupRevenue = new Double(0);
									}
									groupRevenue += revenue;
									groups.put(key, groupRevenue);
								}
							}
						}
					}
				}
			}
		}
		
		List<DBObject> result = new ArrayList<DBObject>();
		for (String key : groups.keySet()) {
			BasicDBObject row = new BasicDBObject("n_name", key);
			row.put("revenue", groups.get(key));
			result.add(row);
		}
		
		Collections.sort(result, new Comparator<DBObject>() {

			@Override
			public int compare(DBObject arg0, DBObject arg1) {
				Double revenue1 = (Double) arg0.get("revenue");
				Double revenue2 = (Double) arg1.get("revenue");
				return revenue2.compareTo(revenue1);
			}
			
		});
		
		Long end = System.nanoTime();
		
		DBCollection resultTable = database.getCollection( "resultQuery4" );
		BasicDBObject resultObject = new BasicDBObject();
		resultObject.put("numResults", result.size());
		resultObject.put("result", result);
		resultTable.insert(resultObject);

		return end-start;
	}

}

