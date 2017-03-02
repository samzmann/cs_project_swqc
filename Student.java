public class Student {

private String id;
private ArrayList<String> Queries = new ArrayList<String>();
private double gpa;
private int totalQueries;
private int totalschoolRelated;
private int totalnonSchoolRelated;
private double  percentSR;
  
public student()
{
  
}

public String getID()
{
  return id;
}
public double getGpa()
{ 
  return gpa;
}
  
public int getTotalQueries()
{
  return totalQueries;
}
 
public int getTotalschoolRelated()
{
   return totalschoolRelated;
}
 
public int gettotalnonSchoolRelated()
{
    return totalnonSchoolRelated;
}
  
public double getPercentSR()
{
  return percentSR();
}
public void addQuery(String s1)
{
    this.Queries.add(s1);
}  
public void toString()
{
}
public static void main(String [] args){
   Student o1= new HelloWorld();
   o1.addQuery("helloworl");
   o1.addQuery("i got it");
   System.out.println(o1.getQueries());
}
}
