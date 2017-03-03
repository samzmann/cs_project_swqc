public class Time {

 private double queryid;
 private String user;
 private String term;
 private String domain;
 private boolean schoolRelated;
 private int count;
 
Query()
{
}

public double getQid()
{
  return queryid;
}
 
public String getUser()
{
 return user;
}

 public String getTerm()
 {
  return term;
 }
  
 public boolean getSchoolRelated()
 {
  return schoolRelated;
 }
 
publc int getCount()
{
 return count;
}

public String getDomain()
{
 return domain;
}
}
