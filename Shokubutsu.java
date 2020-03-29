import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
class Shokubutsu extends JFrame{
	public static final int WORD_COUNT=4759;
	public static final int DAILY_AMOUNT=150;
	public static final double ERR_RATE=1.2;
	public static final int SINGLE_STAGE=5;
	public static final int ADV_COUNT=2469;
	Container con;
	JComponent jc;
	JPanel panel;
	JTextArea wordlabel;
	JTextArea answerlabel;
	JTextArea datalabel;
	JTextArea advertisementlabel;
	int level[];
	String wordlist[];
	String answerlist[];
	String advertisement[];
	int problemid;
	boolean solved[];
	boolean easy[];
	int taskindex[];
	int used[];
	int tasklen;
	boolean firstround;
	int correct;
	Shokubutsu(){
		level=new int[WORD_COUNT];
		wordlist=new String[WORD_COUNT];
		answerlist=new String[WORD_COUNT];
		taskindex=new int[DAILY_AMOUNT];
		solved=new boolean[DAILY_AMOUNT];
		easy=new boolean[DAILY_AMOUNT];
		used=new int[WORD_COUNT];
		advertisement=new String[ADV_COUNT];
		firstround=true;
		problemid=0;
		tasklen=DAILY_AMOUNT;
		correct=0;
		for(int i=0;i<DAILY_AMOUNT;i++)easy[i]=true;
		try{
			File wordfile=new File("barronlist.txt");
			FileReader fr=new FileReader(wordfile);
			BufferedReader br=new BufferedReader(fr);
			String cur=br.readLine();
			int ind=0;
			while(cur!=null){
				wordlist[ind]=cur.split("\\t")[0];
				answerlist[ind++]=cur.split("\\t")[1];
				
				cur=br.readLine();
			}
			br.close();
			File datafile=new File("Shokubutsu.dat");
			fr=new FileReader(datafile);
			br=new BufferedReader(fr);
			cur=br.readLine();
			ind=0;
			while(cur!=null){
				level[ind++]=Integer.parseInt(cur);
				cur=br.readLine();
			}
			br.close();
			File advfile=new File("advertisement.txt");
			fr=new FileReader(advfile);
			br=new BufferedReader(fr);
			cur=br.readLine();
			ind=0;
			while(cur!=null){
				String[] tmp=cur.split("\\t",-1);
				advertisement[ind++]=tmp[1]+" "+tmp[2]+" "+tmp[4]+" "+tmp[6];

				cur=br.readLine();
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		double tv=0;
		for(int i=0;i<WORD_COUNT;i++){
			tv+=Math.pow(ERR_RATE,level[i]);
		}
		for(int i=0;i<DAILY_AMOUNT;i++){
			while(true){
				double rnd=Math.random()*tv;
				int nextind=0;
				for(int j=0;j<WORD_COUNT;j++){
					double v=Math.pow(ERR_RATE,level[j]);
					if(rnd<v){
						nextind=j;break;
					}
					rnd-=v;
				}
				if(used[nextind]==0){
					used[nextind]=1;
					taskindex[i]=nextind;
					break;
				}
			}
		}
	}
	void start(){
		con=getContentPane();
		jc=(JComponent)con;
		jc.getInputMap().put(KeyStroke.getKeyStroke("released J"),"ok");
		jc.getInputMap().put(KeyStroke.getKeyStroke("released F"),"ng");
		jc.getInputMap().put(KeyStroke.getKeyStroke("released SPACE"),"showans");
		jc.getActionMap().put("ok", new AbstractAction(){public void actionPerformed(ActionEvent e){nextQuestion(1);}});
		jc.getActionMap().put("ng", new AbstractAction(){public void actionPerformed(ActionEvent e){nextQuestion(0);}});
		jc.getActionMap().put("showans", new AbstractAction(){public void actionPerformed(ActionEvent e){showAnswer();}});
		

		setSize(640,480);
		setTitle("Shokubutsu");
		
		panel=new JPanel();
		panel.setLayout(null);

		advertisementlabel=new JTextArea();
		advertisementlabel.setBounds(50,140,540,30);
		advertisementlabel.setFont(new Font("Arial",Font.PLAIN,16));
		advertisementlabel.setLineWrap(true);
		advertisementlabel.setWrapStyleWord(true);
		advertisementlabel.setEditable(false);

		wordlabel=new JTextArea();
		wordlabel.setBounds(50,50,540,60);
		wordlabel.setFont(new Font("Arial",Font.PLAIN,20));
		wordlabel.setLineWrap(true);
		wordlabel.setWrapStyleWord(true);
		wordlabel.setEditable(false);

		answerlabel=new JTextArea();
		answerlabel.setBounds(50,200,540,120);
		answerlabel.setFont(new Font("Arial",Font.PLAIN,20));
		answerlabel.setLineWrap(true);
		answerlabel.setWrapStyleWord(true);
		answerlabel.setEditable(false);

		datalabel=new JTextArea();
		datalabel.setBounds(50,350,540,80);
		datalabel.setFont(new Font("Arial",Font.PLAIN,20));
		datalabel.setLineWrap(true);
		datalabel.setWrapStyleWord(true);
		datalabel.setEditable(false);

		panel.add(advertisementlabel);
		panel.add(wordlabel);
		panel.add(answerlabel);
		panel.add(datalabel);
		con.add(panel);
		wordlabel.setText(wordlist[taskindex[problemid]]);
		advertisementlabel.setText("");
		answerlabel.setText("");
		datalabel.setText("Problem: "+problemid+" / 150");

		addWindowListener(new WListener());
		
		setVisible(true);
	}
	void nextQuestion(int ans){
		advertisementlabel.setText("");
		if(ans==1){
			solved[problemid]=true;
		}else{
			easy[problemid]=false;
		}
		int to=-1;
		int cur=problemid;
		for(int i=1;i<=SINGLE_STAGE;i++){
			if((cur+i)%SINGLE_STAGE==0)cur-=SINGLE_STAGE;
			if(cur+i<tasklen&&!solved[cur+i]){
				to=cur+i;break;
			}
		}
		if(to==-1){
			if(problemid-problemid%SINGLE_STAGE+SINGLE_STAGE<tasklen){
				to=problemid-problemid%SINGLE_STAGE+SINGLE_STAGE;
			}else{
				if(firstround){
					firstround=false;
					for(int i=0;i<DAILY_AMOUNT;i++){
						if(easy[i]){
							level[taskindex[i]]--;
							correct++;
						}else level[taskindex[i]]++;
					}
				}
				int ind=0;
				for(int i=0;i<tasklen;i++){
					if(!easy[i]){
						taskindex[ind++]=taskindex[i];
					}
				}
				tasklen=ind;
				if(tasklen>0){
					to=0;
					for(int i=0;i<tasklen;i++){
						int at=(int)(Math.random()*(i+1));
						int tmp=taskindex[i];
						taskindex[i]=taskindex[at];
						taskindex[at]=tmp;
					}
				}
				for(int i=0;i<tasklen;i++){
					solved[i]=false;
					easy[i]=true;
				}
			}
		}
		if(to!=-1){
			problemid=to;
			wordlabel.setText(wordlist[taskindex[problemid]]);
			answerlabel.setText("");
			datalabel.setText("Problem: "+(problemid+1)+" / "+tasklen);
		}else{
			datalabel.setText("Finished: "+correct+" / 150");

			try{
				File file=new File("Shokubutsu.dat");
				FileWriter fw=new FileWriter(file);
				for(int i=0;i<WORD_COUNT;i++){
					fw.write(""+level[i]);
					fw.write("\n");
				}
				fw.close();
				System.out.println("Successfully updated");
			}catch(IOException e){
				e.printStackTrace();
			}
		}

	}
	void showAnswer(){
		advertisementlabel.setText(advertisement[(int)(Math.random()*ADV_COUNT)]);
		answerlabel.setText(answerlist[taskindex[problemid]]);
	}

	public static void main(String args[]){
		Shokubutsu ins=new Shokubutsu();
		ins.start();
	}
}
class WListener extends WindowAdapter{
	public void windowClosing(WindowEvent e){
		System.out.println("END");
		System.exit(0);
	}
}