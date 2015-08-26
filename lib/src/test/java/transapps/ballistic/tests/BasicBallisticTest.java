//******** Tests With Standard Units *********

package transapps.ballistic.tests;

import junit.framework.Assert;
import junit.framework.TestCase;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.data.Coriolis;
import transapps.ballistic.lib.data.Weapon;

@SuppressWarnings("deprecation")
public class BasicBallisticTest extends TestCase {

	Atmosphere atmo = new Atmosphere("air",29.92,59.0,0.0);//checked
	Bullet bullet = new Bullet(null,"bullet","shoot me",4,0.243,0.308,175.0,1.24);//checked
	Weapon weapon = new Weapon(null,"M110","Semi Automatic",2580.0,2,true,11.25,328.0,atmo,bullet);
	Coriolis coriolis = new Coriolis(70.0,4.5,weapon.velocity);
	Ballistics ball = Ballistics.getBallistics(weapon, atmo, 0, 15, 180, 400, Ballistics.UNITS.IMPERIAL
			, 0, coriolis);


	//@Test1: tests gyro value
	public void testGetGyroStability() {
		Double actual = 1.8916652466767492;
		System.out.println(ball.getGyroStability()); // testing
		Assert.assertEquals(ball.getGyroStability(),actual);						
	}

	//@Test3: Tests SpinDriftOn method in Ballistics.java. Should be Off.
	public void testSpinDriftOn() {
		ball.setSpinDrift(false);
		boolean spinOn = ball.spinDriftOn();
		Assert.assertFalse(spinOn);						
	}

	//@Test4: Tests getEnergey method in RangeData.java. Energy in Joules. 
	public void testgetEnergy() {
		Double actual = 2235.590746272341; 
		Double energy = ball.getEnergy(100, 175); 
		System.out.println(energy);
		Assert.assertEquals(energy,actual);						
	}

	//@Test5: Tests isImperial method in Ballistics.java
	public void testImperial(){
		Assert.assertTrue(ball.isImperial());
	}

	//@Test6: Tests the getDrop method in RangeData.java (trajectory)
	public void testDropValue(){
		Double myDrop = -35.28773644654376; 
		Double drop = ball.spinDriftAtZero().getDrop();
		System.out.println(drop);
		Assert.assertEquals(drop,myDrop);						
	}

	//@Test7: Tests spinDriftAtZero in RangeData.java,converts to MOA (trajectory)
	public void testDropinMOA(){
		Double myDrop = -10.269028810459583; //a bit...WRONG!
		Double dropMOA = ball.spinDriftAtZero().getMoa();
		System.out.println(dropMOA);
		Assert.assertEquals(dropMOA,myDrop);						
	}

	//@Test8: Tests spinDriftAtZero in RangeData.java,converts to Mil (trajectory)
	public void testDropinMIL(){
		Double myDrop = -2.9869193747700935; //a bit...WRONG!
		Double dropMil = ball.spinDriftAtZero().getMil();
		System.out.println(dropMil);
		Assert.assertEquals(dropMil,myDrop);						
	}
	//@Test9: Tests static method zeroAngle in Ballistics.java
	public void testZeroAngle(){
		Double zeroAngle = Ballistics.zeroAngle(weapon);
		Double myZero = 0.17121887207031256;
		System.out.println(zeroAngle);
		Assert.assertEquals(myZero,zeroAngle);
	}

	//@Test10: Tests getTime method in RangeData.java at 100 yards
	public void testTimeofFlight(){
		Double time = ball.getData(100).getTime();
		Double myTime = 0.12078697626357383;
		System.out.println(time);
		Assert.assertEquals(time, myTime);

	}

	//@Test11: Tests getTime method in RangeData.java at 400 yards
	public void testTimeofFlightEnd(){
		Double time1 = ball.getData(400).getTime();
		Double myTime = 0.5423322262706927;
		System.out.println(time1);
		Assert.assertEquals(time1, myTime);

	}

	//@Test12: Tests getVelocity method in RangeData.java at 100 yards
	public void testgetVelocity(){
		Double vel = ball.getData(100).getVelocity();
		Double myVel = 2398.7979768303344;
		System.out.println(vel);
		Assert.assertEquals(vel, myVel);
	}

	//@Test13: Tests getVelocity method in RangeData.java at 400 yards
	public void testgetVelocityEnd(){
		Double vel1 = ball.getData(400).getVelocity();
		Double myVel = 1901.4487781803423;
		System.out.println(vel1);
		Assert.assertEquals(vel1, myVel);
	}
}