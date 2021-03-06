/* 
    Copyright (c) Marcelo Barros de Almeida <marcelobarrosalmeida@gmail.com>

    This work is licensed under a Creative Commons 
    Attribution-ShareAlike 3.0 Unported License:

    http://creativecommons.org/licenses/by-sa/3.0/
 
 */
package com.instantme.forms;

import com.instantme.InstantME;
import com.instantme.api.InstagramAPI;
import com.instantme.entries.PhotoEntry;
import com.instantme.entries.TaskEntry;
import com.instantme.entries.UserEntry;
import com.instantme.items.AdImageItem;
import com.instantme.items.PhotoItem;
import com.instantme.items.WaitItem;
import com.instantme.locales.Locale;
import com.instantme.model.DataModel;
import com.instantme.model.PhotoEntryModel;
import com.instantme.util.BackStack;
import com.instantme.util.IAnimation;
import com.instantme.util.IDetails;
import com.instantme.util.TaskHelper;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import com.nokia.mid.ui.IconCommand;
import com.nokia.mid.ui.LCDUIUtil;
//import com.nokia.mid.ui.LCDUIUtil;

public class PhotoListForm extends Form implements Runnable, IAnimation, IDetails, ItemCommandListener, CommandListener {

    private PhotoEntryModel model;
    private TextBox helpBox = null;
    private IconCommand updateCommand;// = new Command(Locale.getInst().getStr(Locale.UPDATE_MENU), Command.OK, 0);
    private Command olderCommand = new Command(Locale.getInst().getStr(Locale.OLDER_MENU), Command.ITEM, 1);
    private Command newerCommand = new Command(Locale.getInst().getStr(Locale.NEWER_MENU), Command.ITEM, 2);
    private Command friendsCommand = new Command(Locale.getInst().getStr(Locale.FRIENDS_MENU), Command.ITEM, 3);
    private Command loginCommand = new Command(Locale.getInst().getStr(Locale.LOGIN_MENU), Command.ITEM, 4);
    private Command meCommand = new Command(Locale.getInst().getStr(Locale.ME_MENU), Command.ITEM, 5);
    private Command aboutCommand= new Command(Locale.getInst().getStr(Locale.ABOUT_MENU), Command.ITEM, 6);
    private Command exitCommand = new Command(Locale.getInst().getStr(Locale.EXIT_MENU), Command.EXIT, 7);
    private Command helpCommand = new Command(Locale.getInst().getStr(Locale.HELP_MENU), Command.HELP, 8);
    private Command adCommand = new Command(Locale.getInst().getStr(Locale.OPEN_MENU), Command.ITEM, 0);
    private Command backCommand = new Command(Locale.getInst().getStr(Locale.BACK_MENU), Command.BACK, 0);
    //private Alert alert;
    private WaitItem waitAnim;
    private TaskHelper tasks = null;
    private final static int TASK_LOGIN = 0;
    private final static int TASK_UPDATE_FEED = 1;
    private final static int TASK_GET_PREV_FEED = 2;
    private final static int TASK_GET_NEXT_FEED = 3;

    private AdImageItem adItem;
    
    public PhotoListForm(String title) {
        super(title);
        model = new PhotoEntryModel();

        Image image = null;        
        try {
            image = Image.createImage("/res/refreshhb.png");
            updateCommand = new IconCommand(Locale.getInst().getStr(Locale.UPDATE_MENU), image, null, Command.OK, 0);
        }
        catch (Exception oe) {    
            updateCommand = new IconCommand(Locale.getInst().getStr(Locale.UPDATE_MENU), Command.OK, 0, IconCommand.ICON_OK);
        }

        addCommand(updateCommand);
        addCommand(olderCommand);
        addCommand(newerCommand);
        addCommand(friendsCommand);
        addCommand(meCommand);
        addCommand(loginCommand);
        addCommand(aboutCommand);
        addCommand(exitCommand);
        addCommand(helpCommand);

        setCommandListener(this);

        adItem = new AdImageItem("",null,Item.LAYOUT_EXPAND,"");
        adItem.setDefaultCommand(adCommand);
        adItem.setItemCommandListener(this);
        
        waitAnim = new WaitItem();
        tasks = new TaskHelper(this, this);

        tasks.push(new TaskEntry(TASK_LOGIN));
    }

    public void updateList() {
        // TODO: fazer apenas o novo set do item, sem apagar

        if(model.historySize() > 1) {
            StringItem b1 = new StringItem("", Locale.getInst().getStr(Locale.NEWER_MENU), StringItem.BUTTON);
            b1.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_BEFORE);
            b1.setDefaultCommand(newerCommand);
            b1.setItemCommandListener(this);
            append(b1);
        }
        
        int numElements = model.size();
        Random r = new Random();
        int adPos = r.nextInt(numElements);
        for (int n = 0; n < numElements; n++) {
            PhotoItem pe = new PhotoItem(model.elementAt(n), this);
            pe.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_BEFORE);
            append(pe);
            LCDUIUtil.setObjectTrait(pe, "nokia.ui.s40.item.direct_touch",  Boolean.TRUE);
            if(n == adPos) {
                addAdv();
            }
        }

        StringItem b2 = new StringItem("", Locale.getInst().getStr(Locale.OLDER_MENU), StringItem.BUTTON);
        b2.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_BEFORE);
        b2.setDefaultCommand(olderCommand);
        b2.setItemCommandListener(this);
        append(b2);
    }

    public void showDetails(PhotoEntry pe) {
        PhotoEntryForm d = new PhotoEntryForm(pe);
        BackStack bs = BackStack.getInstance();
        bs.forward(d);
        // TODO item is not painted again when changed
    }

    private void addAdv() {
        BackStack bs = BackStack.getInstance();
        Vector v = InneractiveSDK.IADView.getBannerAdData(bs.getRunningMidlet());            
        if(v != null){
            Image img = (Image) v.elementAt(0);
            String url = (String) v.elementAt(1);
            if(img != null) {
                adItem.setImage(img);
                adItem.setLabel(url);
                append(adItem);
                try {
                    LCDUIUtil.setObjectTrait(adItem, "nokia.ui.s40.item.direct_touch",  Boolean.TRUE);
                } catch(Exception e) {
                    System.out.println(e);
                }
            }
        }        
    }
    public void commandAction(Command c, Item item) {
        BackStack bs = BackStack.getInstance();
        if (tasks.isRunning()) {
            showAlert(Locale.getInst().getStr(Locale.WAIT), Locale.getInst().getStr(Locale.WAIT_OPERATION));
        } else {
            if (c == olderCommand) {
                deleteAll();
                tasks.push(new TaskEntry(TASK_GET_PREV_FEED));
            } else if (c == newerCommand) {
                deleteAll();
                tasks.push(new TaskEntry(TASK_GET_NEXT_FEED));
            } else if(c == adCommand) {
                try {
                    String url = adItem.getLabel();
                    if(url != null) {
                        if(url.length() > 0) {
                            bs.getRunningMidlet().platformRequest(url);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    public void commandAction(Command c, Displayable d) {

        BackStack bs = BackStack.getInstance();

        if (c == exitCommand) {
            ((InstantME) bs.getRunningMidlet()).exitMIDlet();
        } else if (c == aboutCommand) {
            showAlert(Locale.getInst().getStr(Locale.ABOUT), Locale.getInst().getStr(Locale.SUPPORT));
        } else if (c == backCommand) {
            bs.back();
        } else if (tasks.isRunning()) {
            showAlert(Locale.getInst().getStr(Locale.WAIT), Locale.getInst().getStr(Locale.WAIT_OPERATION));
        } else {
            if (c == loginCommand) {
                LoginForm f = new LoginForm();
                bs.forward(f);
            } else if (c == helpCommand) {
                if (helpBox == null) {
                    helpBox = new TextBox(Locale.getInst().getStr(Locale.HELP), Locale.getInst().getStr(Locale.HELP_TEXT), 1024, TextField.UNEDITABLE);
                    helpBox.addCommand(backCommand);
                    helpBox.setCommandListener(this);
                    helpBox.insert("",0);
                }
                bs.forward(helpBox);                
            } else {
                InstagramAPI oai = InstagramAPI.getInstance();
                if (!oai.isLogged()) {
                    showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.LOGIN_FIRST));
                } else {
                    if (c == updateCommand) {
                        deleteAll();
                        tasks.push(new TaskEntry(TASK_UPDATE_FEED));
                    } else if (c == friendsCommand) {
                        RelationshipForm f = new RelationshipForm();
                        bs.forward(f);
                    } else if (c == meCommand) {
                        UserEntry ue = oai.getAuthUserInfo();
                        UserEntryForm f = new UserEntryForm(Locale.getInst().getStr(Locale.MY_INFO), ue);
                        bs.forward(f);
                    } else if (c == olderCommand) {
                        deleteAll();
                        tasks.push(new TaskEntry(TASK_GET_PREV_FEED));
                    } else if (c == newerCommand) {
                        deleteAll();
                        tasks.push(new TaskEntry(TASK_GET_NEXT_FEED));
                    }

                }
            }
        }
    }

    private void showAlert(String title, String msg) {
        BackStack bs = BackStack.getInstance();
        Alert a = new Alert(title);
        a.setString(msg);
        a.setType(AlertType.INFO);
        bs.getCurrentDisplay().setCurrent(a,this);
    }

    public void run() {
        InstagramAPI oai = InstagramAPI.getInstance();
        TaskEntry te = (TaskEntry) tasks.pop();

        // TODO Bloquear opções enquanto uma está pendente !!!!
        switch (te.getID()) {

            case TASK_LOGIN:
                DataModel dm = DataModel.getInstance();
                String tok = dm.getToken();
                String uid = dm.getUserID();
                if (tok.length() == 0 || uid.length() == 0) {
                    showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.PROVIDE_CRED));
                } else {
                    updateProgress(Locale.getInst().getStr(Locale.LOGGING));
                    if (oai.tokenLogin(tok, uid)) {
                        tasks.push(new TaskEntry(TASK_UPDATE_FEED));
                    } else {
                        showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.RESTART_APP));
                    }
                }
                break;

            case TASK_UPDATE_FEED:
                if (oai.isLogged()) {
                    updateProgress(Locale.getInst().getStr(Locale.UPDATING_FEED));
                    if (oai.getSelfFeed(model, 10, this)) {
                        updateList();
                    } else {
                        showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.CAN_NOT_UPDATE));
                    }
                } else {
                    showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.LOGIN_FIRST));
                }
                break;

            case TASK_GET_PREV_FEED:                
                if (oai.isLogged()) {
                    updateProgress(Locale.getInst().getStr(Locale.UPDATING_FEED));
                    if (oai.getNextSelfFeed(model, this)) {
                        updateList();
                    } else {
                        showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.CAN_NOT_UPDATE));
                    }
                } else {
                    showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.LOGIN_FIRST));
                }
                break;

            case TASK_GET_NEXT_FEED:
                if (oai.isLogged()) {
                    updateProgress(Locale.getInst().getStr(Locale.UPDATING_FEED));
                    if (oai.getPrevSelfFeed(model, this)) {
                        updateList();
                    } else {
                        showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.CAN_NOT_UPDATE));
                    }
                } else {
                    showAlert(Locale.getInst().getStr(Locale.FAILED), Locale.getInst().getStr(Locale.LOGIN_FIRST));
                }
                break;

            default:
                break;

        }
    }

    public void start() {
        BackStack bs = BackStack.getInstance();
        insert(0,waitAnim);
        bs.getCurrentDisplay().setCurrentItem(waitAnim);
        waitAnim.start();
    }

    public void stop() {
        delete(0);
        waitAnim.updateProgress("");
        waitAnim.stop();
    }

    public void updateProgress(String msg, int perc) {
        waitAnim.updateProgress(msg);
    }

    public void updateProgress(String msg) {
        waitAnim.updateProgress(msg);
    }

}
