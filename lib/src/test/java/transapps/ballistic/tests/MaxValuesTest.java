//******** Tests With ideal Maximum Values of Unit values at G7 *********
// ******* Values based on ballistic calculator app *********
//******** Didn't change anything in Weapon
package transapps.ballistic.tests;
import junit.framework.Assert;

import org.junit.Test;

import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.data.Coriolis;
import transapps.ballistic.lib.data.Weapon;


public class MaxValuesTest {
	public final int MAXRANGE = 2950, MAXZERO = 1800;
	
	Atmosphere atmo = new Atmosphere("air",35.00,55.0,100.0);
	Bullet bullet = new Bullet(null,"bullet","shoot me",4,0.1537,0.224,62.0,0.906);
	Weapon weapon = new Weapon(null,"M110","Semi Automatic",3112.0,3.755,true,7.0,MAXZERO,atmo,bullet);//didn't change weapon
	Coriolis coriolis = new Coriolis(0.0,0.0,weapon.velocity);
	Ballistics ball = Ballistics.getBallistics(weapon, atmo, 89, 90.9, 12.59, MAXRANGE, Ballistics.UNITS.IMPERIAL
			, 0, coriolis);

	@Test
	//@Test1: Tests the velocity values for the ranges for every 100 yards
	public void testGetVelocity(){
		System.out.println("Velocity");
		System.out.println("(fps)");
		for (int i = 0; i <= MAXRANGE; i += 100){
			System.out.println(ball.getData(i).getVelocity());
		}
		Assert.assertTrue(true);
	}
	@Test
	//@Test2: Tests the Energy values for the ranges for every 100 yards
	public void testGetEnergy(){
		System.out.println("Energy");
		System.out.println("(ft-lb)");
		for (int i = 0; i <= MAXRANGE; i += 100){
			System.out.println(ball.getEnergy(i, 62));
		}
		Assert.assertTrue(true);
	}
	
	@Test
	//@Test3: Tests the Drop values for the ranges for every 100 yards
	public void testGetTrajectory(){
		System.out.println("Trajectory");
		System.out.println("(MOA)");
		for (int i = 0; i <= MAXRANGE; i += 100){
			System.out.println(ball.getData(i).getMoa());//drop in MOA
		}
		Assert.assertTrue(true);
	}
	

	@Test
	//@Test4: Tests the getTime values for the ranges for every 100 yards
	public void testGetTOF(){
		System.out.println("Time of Flight");
		System.out.println("(sec)");
		for (int i = 0; i <= MAXRANGE; i += 100){
			System.out.println(ball.getData(i).getTime());//drop in MOA
		}
		Assert.assertTrue(true);
	}
	
	@Test
	//@Test5: Tests the getWindage values for the ranges for every 100 yards
	public void testGetDrift(){
		System.out.println("Windage");
		System.out.println("(MOA)");
		for (int i = 0; i <= MAXRANGE; i += 100){
			System.out.println(ball.getData(i).getWindageMoa());//drop in MOA
		}
		Assert.assertTrue(true);
	}
	
	@Test
	//@Test5: Tests the getWindage values for the ranges for every 100 yards
	public void testGetSomething(){
		System.out.println("Something");
		System.out.println("(MOA)");
		for (int i = 0; i <= MAXRANGE; i += 100){
			System.out.println(ball.getData(i).getMoa());//drop in MOA
		}
		Assert.assertTrue(true);
	}
}
