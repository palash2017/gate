package gate.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class WaitDialog extends JWindow implements Runnable{
  Box centerBox;

  public WaitDialog(Frame frame, String title, boolean modal) {
    super(frame);
    this.frame = frame;
    try  {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public synchronized void showDialog(String[] texts, boolean modal){
    centerBox.removeAll();
    for(int i =0; i < texts.length; i++){
      centerBox.add(new JLabel(texts[i]));
    }
    centerBox.validate();
    pack();
    Point loc = frame.getLocation();
    loc.move(frame.getSize().width - getSize().width / 2 ,
             frame.getSize().height - getSize().height /2 );
    setLocation(loc);
    stop = false;
    Thread thread = new Thread(this);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
    show();
  }

  public synchronized void showDialog(Component[] components, boolean modal){
    centerBox.removeAll();
    for(int i =0; i < components.length; i++){
      centerBox.add(components[i]);
    }
    centerBox.validate();
    pack();
    Point loc = frame.getLocation();
    setLocation(loc.x + (frame.getSize().width - getSize().width) / 2 ,
                loc.y + (frame.getSize().height - getSize().height) /2);
    stop = false;
    Thread thread = new Thread(this);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
    show();
  }

  void jbInit() throws Exception {
    JPanel centerPanel = new JPanel();
    centerBox = Box.createVerticalBox();
    centerPanel.setLayout(borderLayout1);
//    centerPanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
//    centerPanel.setBorder(new CompoundBorder(new LineBorder(Color.darkGray, 2),
//                                    new SoftBevelBorder(BevelBorder.LOWERED)));
    centerPanel.setBorder(new LineBorder(Color.darkGray, 2));
    centerPanel.setBackground(Color.white);
    centerBox.setBackground(Color.white);
    picture = new JLabel(new ImageIcon(ClassLoader.getSystemResource(
                    "muse/resources/wait.gif")));
    centerPanel.add(centerBox, BorderLayout.CENTER);
    centerPanel.add(picture, BorderLayout.WEST);
    centerPanel.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
    centerPanel.add(Box.createVerticalStrut(5), BorderLayout.SOUTH);
    centerPanel.add(Box.createHorizontalStrut(8), BorderLayout.EAST);
    getContentPane().add(centerPanel, BorderLayout.CENTER);

  }

  public void goAway(){
    stop = true;
  }
  public void run(){
    while(!stop){
      try{
        Thread.sleep(200);
        picture.paintImmediately(picture.getVisibleRect());
      }catch(InterruptedException ie){}
    }
    this.setVisible(false);
  }

  boolean stop = false;
  BorderLayout borderLayout1 = new BorderLayout();
  Frame frame;
  JLabel picture;
}
