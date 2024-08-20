import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import static java.lang.String.format;

public class BuildingGrid {
    JFrame f;
    TableModel tm;
    BuildingGrid(int NUMFLOORS, int NUMROOMS){
        f=new JFrame();
        Object objects[][];
        String headings[];

        objects = new Object[NUMFLOORS+1][NUMROOMS+4];
        headings = new String[NUMROOMS+4];

        headings[0] = "v Floor | Room >";
        headings[NUMROOMS+4-1] = "< Room | Floor v";
        for (int i = 0; i < NUMROOMS+2; i++) {
            headings[i+1] = Integer.toString(i);
        }

        for (int i = 0; i < NUMFLOORS+1; i++) {
            objects[NUMFLOORS+1-i-1][0] = Integer.toString(i);
            objects[NUMFLOORS+1-i-1][NUMROOMS+4-1] = Integer.toString(i);
        }

        for (int i = 0; i < NUMFLOORS+1; i++) {
            objects[i][1] = "#"; // ladder
            objects[i][NUMROOMS+4-2] = "#"; // ladder
        }
        for (int i = 2; i < NUMROOMS+4-2; i++)
            for (int j = 0; j < NUMFLOORS; j++) {
            objects[j][i] = format(" Rm %d.%d", NUMFLOORS-j, i-1); // door
        }

        objects[NUMFLOORS][2] = "Mailroom ...";

                tm = new DefaultTableModel(objects,headings);
        JTable jt = new JTable(tm);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        jt.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        jt.getColumnModel().getColumn(0).setPreferredWidth(90);
        jt.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        jt.getColumnModel().getColumn(1).setPreferredWidth(25);
        jt.getColumnModel().getColumn(NUMROOMS+4-2).setCellRenderer(centerRenderer);
        jt.getColumnModel().getColumn(NUMROOMS+4-2).setPreferredWidth(25);
        jt.getColumnModel().getColumn(NUMROOMS+4-1).setCellRenderer(centerRenderer);
        jt.getColumnModel().getColumn(NUMROOMS+4-1).setPreferredWidth(90);

        jt.setBounds(100,100,600,600);
        JScrollPane sp=new JScrollPane(jt);
        f.setTitle("AutoMail");
        f.add(sp);
        f.setSize(600,600);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        }

    void update(int floor, int room, String s) {
        String sfinal;
        if (room == 0 || room == Building.getBuilding().NUMROOMS+1) {
            sfinal = s.isEmpty() ? "#" : s;  // ladder
        } else {
            String tail = s.isEmpty() ? "" : format(" [%s]", s);
            sfinal = format(" Rm %d.%d%s", floor, room, tail); // door
        }
        tm.setValueAt(sfinal, Building.getBuilding().NUMFLOORS - floor, room + 1);
    }

}
