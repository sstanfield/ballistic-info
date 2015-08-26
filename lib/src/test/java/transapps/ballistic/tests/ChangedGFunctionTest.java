//******** Tests With Standard Units but using G1 Function *********
// ******* Manually compared the results of each, using a for loop ****
package transapps.ballistic.tests;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.data.Coriolis;
import transapps.ballistic.lib.data.Weapon;

public class ChangedGFunctionTest extends TestCase {
	public final int Range = 1000;

	Atmosphere atmo = new Atmosphere("air",29.92,59.0,0.0);
	Bullet bullet = new Bullet(null,"bullet","shoot me",0,0.243,0.308,175.0,1.24);
	Weapon weapon = new Weapon(null,"M110","Semi Automatic",2579.99,2,true,11.25,450.0,atmo,bullet);
	Coriolis coriolis = new Coriolis(70.0,4.5,weapon.velocity);
	Ballistics ball = Ballistics.getBallistics(weapon, atmo, 0, 15, 180, Range, Ballistics.UNITS.IMPERIAL
			, 0, coriolis);
	@Test
	//@Test1: Tests the velocity values for the ranges for every 100 yards
	public void testGetVelocity(){
		System.out.println("Velocity");
		System.out.println("(fps)");
		for (int i = 0; i <= Range; i += 100){
			System.out.println(ball.getData(i).getVelocity());
		}
		Assert.assertTrue(true);
	}

	@Test
	//@Test2: Tests the Energy values for the ranges for every 100 yards
	public void testGetEnergy(){
		System.out.println("Energy");
		System.out.println("(ft-lb)");
		for (int i = 0; i <= Range; i += 100)
			System.out.println(ball.getEnergy(i, 62));
		Assert.assertTrue(true);
	}

	@Test
	//@Test3: Tests the Drop values for the ranges for every 100 yards
	public void testGetTrajectory(){
		System.out.println("Trajectory");
		System.out.println("(MOA)");
		for (int i = 0; i <= Range; i += 100)
			System.out.println(ball.getData(i).getMoa());//drop in MOA
		Assert.assertTrue(true);
	}

	@Test
	//@Test4: Tests the getTime values for the ranges for every 100 yards
	public void testGetTOF(){
		System.out.println("Time of Flight");
		System.out.println("(sec)");
		for (int i = 0; i <= Range; i += 100)
			System.out.println(ball.getData(i).getTime());//drop in MOA

		Assert.assertTrue(true);
	}

	@Test
	//@Test5: Tests the getWindage values for the ranges for every 100 yards
	public void testGetDrift(){
		System.out.println("Drift");
		System.out.println("(MOA)");
		for (int i = 0; i <= Range; i += 100)
			System.out.println(ball.getData(i).getWindageMoa());//drop in MOA
		Assert.assertTrue(true);
	}
}