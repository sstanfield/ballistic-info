package transapps.ballistic.app;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import transapps.ballistic.db.dvo.WeaponDVO;
import transapps.ballistic.lib.data.Weapon;

public class Data {
	public static Weapon[] defaultWeapons = WeaponDVO.allWeaponsArray();

	public static ComboBoxModel getWeaponComboBoxModel(final int sel) {
		defaultWeapons = WeaponDVO.allWeaponsArray();
		return new ComboBoxModel() {
			private int selected = sel;

			@Override
			public int getSize() {
				return Data.defaultWeapons.length;
			}

			@Override
			public Object getElementAt(int index) {
				return Data.defaultWeapons[index];
			}

			@Override
			public void addListDataListener(ListDataListener l) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void removeListDataListener(ListDataListener l) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSelectedItem(Object anItem) {
				for (int c = 0; c < Data.defaultWeapons.length; c++) {
					if (Data.defaultWeapons[c] == anItem) {
						selected = c;
						break;
					}
				}
			}

			@Override
			public Object getSelectedItem() {
				return Data.defaultWeapons[selected];
			}
		};
	}
}
