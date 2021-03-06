package com.customwars.ui.menu;
/*
 *ContextMenu.java
 *Author: Urusan
 *Contributors: Adam Dziuk
 *Creation: July 14, 2006, 10:04 AM
 *This menu pops up when a unit finishes moving, and lets the user select an action
 *It has several possible combinations depending on the context
 */

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.customwars.map.Map;
import com.customwars.map.location.Invention;
import com.customwars.map.location.Location;
import com.customwars.map.location.Property;
import com.customwars.map.location.Silo;
import com.customwars.sfx.SFX;
import com.customwars.state.ResourceLoader;
import com.customwars.unit.Carrier;
import com.customwars.unit.Stealth;
import com.customwars.unit.Submarine;
import com.customwars.unit.Transport;
import com.customwars.unit.UNIT_COMMANDS;
import com.customwars.unit.Unit;
import com.customwars.unit.UnitID;

public class ContextMenu extends InGameMenu {
    private static final int CONTEXT_MENU_X = 192;
	private static final int CONTEXT_MENU_Y = 120;
	private static final int WIDTH_OF_CONTEXT_MENU = 104;
	Unit u;
	final static Logger logger = LoggerFactory.getLogger(ContextMenu.class); 
    
    //[NEW]
    private static ArrayList<Location> contextTargs = new ArrayList<Location>();
    
    //constructor
    public ContextMenu(Unit temp, boolean fire, boolean capture, boolean resupply, boolean unload, boolean unload2, boolean repair, boolean launch, boolean explode, boolean dive, boolean rise, boolean hide, boolean appear, boolean join, boolean load, boolean special1, boolean special2, boolean takeoff, boolean takeoff2, boolean build, ImageObserver screen){
        super(CONTEXT_MENU_X,CONTEXT_MENU_Y,WIDTH_OF_CONTEXT_MENU,screen);
        String[] s = new String[6]; //max in one context window is 4 (Black Boat, Cruiser, and Carrier)
        //Expanded for time being.
        int i = 0;
        u = temp;
        if((temp.getArmy().getBattle().getMap().find(temp).getTerrain().getName().equals("Wall"))
            || (temp.getUType() == UnitID.CARRIER && ((Carrier)temp).isLaunched()) && temp.getMoved()) {
            s[0] = "No.";
            i++;
        }else{
            if(join == false && load == false){
                if(fire){s[i]="Fire";i++;}
                if(capture){s[i]="Capture";i++;}
                if(unload){s[i]="Unload";i++;}
                if(unload2){s[i]="Unload";i++;}
                if(resupply){s[i]="Resupply";i++;}
                if(repair){s[i]="Repair";i++;}
                if(launch){s[i]="Launch";i++;}
                if(explode){s[i]="Explode";i++;}
                if(dive){s[i]="Dive";i++;}
                if(rise){s[i]="Rise";i++;}
                if(hide){s[i]="Hide";i++;}
                if(appear){s[i]="Appear";i++;}
                if(special1){s[i] = u.getArmy().getCO().special1;i++;}
                if(special2){s[i] = u.getArmy().getCO().special2;i++;}
                if(takeoff){s[i]="Takeoff";i++;}
                if(takeoff2){s[i]="Takeoff";i++;}
                if(build){s[i]="Build";i++;}
                if(!u.isNoWait() && (!temp.getArmy().getBattle().getMap().find(temp).getTerrain().getName().equals("Wall")) && !(temp.getUType() == UnitID.CARRIER && ((Carrier)temp).isLaunched())){s[i] = "Wait"; i++;}
            }else if(join == true){
                s[0] = "Join";i++;
            }else{
                s[0] = "Load";i++;
            }
            if(i == 0) { //nothing in the list? No!
                s[0] = "No.";
                i++;
            }
        }
        String[] s2 = new String[i];
        for(int j=0;j<i;j++)s2[j]=s[j];
        super.loadStrings(s2);
    }
    
    public int doMenuItem(){
       	String soundLocation = ResourceLoader.properties.getProperty("soundLocation");
        SFX.playClip(soundLocation + "/ok.wav");
        if(displayItems[item].equals("Wait")){
            logger.info("Unit selected to wait");
            return 0;
        }else if(displayItems[item].equals("Fire")){
            logger.info("Unit selected to Fire");
            return 1;
        }else if(displayItems[item].equals("Capture")){
            logger.info("Unit selected to Capture");
            return 2;
        }else if(displayItems[item].equals("Resupply")){
            logger.info("Unit selected to Resupply");
            return 3;
        }else if(displayItems[item].equals("Unload")){
            if(item-1>=0){
                if(displayItems[item-1].equals("Unload")){
                    logger.info("Unit selected to Unload #2");
                    return 7;
                }
            }
            logger.info("Unload #1");
            return 6;
        }else if(displayItems[item].equals("Repair")){
            logger.info("Unit selected to Repair");
            return 10;
        }else if(displayItems[item].equals("Launch")){
            logger.info("Unit selected to Launch");
            return 8;
        }else if(displayItems[item].equals("Explode")){
            logger.info("Unit selected to Explode");
            return 9;
        }else if(displayItems[item].equals("Join")){
            logger.info("Unit selected to Join");
            return 4;
        }else if(displayItems[item].equals("Load")){
            logger.info("Unit selected to Load");
            return 5;
        }else if(displayItems[item].equals("Dive")){
            logger.info("Unit selected to Dive");
            return 11;
        }else if(displayItems[item].equals("Rise")){
            logger.info("Unit selected to Rise");
            return 12;
        }else if(displayItems[item].equals("Hide")){
            logger.info("Unit selected to Hide");
            return 13;
        }else if(displayItems[item].equals("Appear")){
            logger.info("Unit selected to Appear");
            return 14;
        }else if(displayItems[item].equals(u.getArmy().getCO().special1)){
            logger.info(u.getArmy().getCO().special1);
            return 22;
        }else if(displayItems[item].equals(u.getArmy().getCO().special2)){
            logger.info("Unit selected to "+displayItems[item].equals(u.getArmy().getCO().special2));
            return 23;
        }else if(displayItems[item].equals("Takeoff")){
            if(item-1>=0){
                if(displayItems[item-1].equals("Takeoff")){
                    logger.info("Unit selected to Takeoff #2");
                    return UNIT_COMMANDS.LAUNCH2;
                }
            }
            logger.info("Takeoff #1");
            return UNIT_COMMANDS.LAUNCH;
        }else if(displayItems[item].equals("Build")){
            logger.info("Unit selected to Build");
            return UNIT_COMMANDS.BUILD;
        }else if(displayItems[item].equals("No.")){
            logger.info("Unit selected Invalid move");
        }else{
            System.err.println("ERROR, INVALID CONTEXT MENU ITEM");
        }
        return -1;
    }
    
    public static ContextMenu generateContext(Unit u, boolean join, boolean load, boolean secondunload, boolean secondTakeoff, ImageObserver screen){
        boolean fire=false, capture=false, supply=false, unload=false, unload2=false, repair=false, launch=false, explode=false, dive=false, rise=false, hide=false, appear=false, special1 = false, special2=false, takeoff = false, takeoff2 = false, build = false;
        Map m = u.getMap();
        
        if(join)return new ContextMenu(u,fire,capture,supply,unload,unload2,repair,launch,explode,dive,rise,hide,appear,true,false,false, false, false, false, false,screen);
        if(load)return new ContextMenu(u, fire,capture,supply,unload,unload2,repair,launch,explode,dive,rise,hide,appear,false,true,false, false, false, false, false,screen);
        
        contextTargs = new ArrayList<Location>();
        
        //FIRE
        boolean canFire = true;
        //prevents indirects from firing
        if(u.getMoved())
            if(u.getMinRange()>1)
                canFire = false;
        if(canFire && !u.isNoFire()) {
            for(int i=0;i<m.getMaxCol();i++) {
                for(int j=0;j<m.getMaxRow();j++) {
                    //if enemy unit in firing range, add fire to the context menu
                    if(u.checkFireRange(new Location(i,j))&&
                            ((m.find(new Location(i,j)).hasUnit())&&
                            (m.find(new Location(i,j)).getUnit().getArmy().getSide()!=u.getArmy().getSide()))) {
                        if(u.displayDamageCalc(m.find(new Location(i,j)).getUnit())>-1 && !m.find(new Location(i,j)).getUnit().isHidden()) {
                            //[NEW]
                            //Add each target's location to the list
                            contextTargs.add(new Location(i,j));
                            fire=true;
                        }
                    }
                    //check for neutral or enemy Inventions
                    if(u.checkFireRange(new Location(i,j)) && m.find(new Location(i,j)).getTerrain() instanceof Invention) {
                        if(u.damageCalc((Invention)m.find(new Location(i,j)).getTerrain())!=-1) {
                            //[NEW]
                            //Add each target's location to the list
                            contextTargs.add(new Location(i,j));
                            fire=true;
                        }
                    }
                }
            }
        }
        

        //CAPTURE & LAUNCH
        //is the unit an infantry type?
        if(u.getUType()==0 || u.getUType()==1){
            //is the terrain a property?
            if(m.find(u.getLocation()).getTerrain() instanceof Property){
                Property p = (Property)m.find(u.getLocation()).getTerrain();
                if(p.isCapturable() && !u.isNoCapture()){
                    if(p.getOwner()!=null){
                        if(p.getOwner().getSide()!=u.getArmy().getSide())
                            capture = true;
                    }else{
                        capture = true;
                    }
                }else{
                    if(!((Silo)p).isLaunched() && !u.isNoLaunch())launch = true;
                }
            }
        }
        
        //SUPPLY
        //is the unit an APC?
        if(u.getUType()==9 && !u.isNoResupply()){
            for(int i=0;i<m.getMaxCol();i++){
                for(int j=0;j<m.getMaxRow();j++){
                    //if friendly in resupply range, add resupply to the context menu
                    if(u.checkAdjacent(new Location(i,j))&&
                            (m.find(new Location(i,j)).hasUnit()&&
                            m.find(new Location(i,j)).getUnit().getArmy()==u.getArmy()
                            && !m.find(new Location(i,j)).getUnit().isNoResupplied()))
                        supply=true;
                }
            }
            //supply=true;
        }
        
        //REPAIR
        //Is the unit a Black Boat?
        if(u.getUType()==21 && !u.isNoRepair()){
            for(int i=0;i<m.getMaxCol();i++){
                for(int j=0;j<m.getMaxRow();j++){
                    //if friendly in repair range, add repair to the context menu
                    if(u.checkAdjacent(new Location(i,j))&&
                            (m.find(new Location(i,j)).hasUnit()&&
                            m.find(new Location(i,j)).getUnit().getArmy()==u.getArmy()
                            && !m.find(new Location(i,j)).getUnit().isNoResupplied()
                            && !m.find(new Location(i,j)).getUnit().isNoRepaired()))
                        repair=true;
                }
            }
            //repair = true;
        }
        
        //EXPLODE
        //is the unit a Black Bomb?
        if(u.getUType()==24 && !u.isNoExplode()){
            explode = true;
        }
        
        //DIVE/RISE
        //Is the unit a Submarine? And if so, are its abilities disabled?
        if(u.getUType()==12) {
            if(((Submarine)u).isDived() && !u.isNoRise()) {
                rise = true;
            } else if(!u.isNoDive()) {
                dive = true;
            }
        }
        
        //HIDE/APPEAR
        //Is the unit a Stealth? And if so, are its abilities disabled?
        if(u.getUType()==23) {
            if(((Stealth)u).isDived() && !u.isNoAppear()) {
                appear = true;
            } else if(!u.isNoHide()) {
                hide = true;
            }
        }
        //Special 1
        if(u.getArmy().getCO().canUseSpecial1(u) && !u.isNoSpecial1())
            special1 = true;
        //special 2
        if(u.getArmy().getCO().canUseSpecial2(u) && !u.isNoSpecial2())
            special2 = true;
        
        if(u.getUType()==UnitID.CARRIER && ((Transport)u).getUnitsCarried() > 0 && !u.getMoved()) {
            if(u instanceof Transport && !u.isNoUnload()){
                Transport trans = (Transport) u;
                int x = trans.getLocation().getCol();
                int y = trans.getLocation().getRow();
                
                if(trans.getUnitsCarried() == 2){
                    if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(2).getMType())!=-1){
                        takeoff2 = true;
                        if(!trans.checkUnloadRange(new Location(x,y+1),2)&&!trans.checkUnloadRange(new Location(x,y-1),2)&&!trans.checkUnloadRange(new Location(x+1,y),2)&&!trans.checkUnloadRange(new Location(x-1,y),2)){
                            takeoff2 = false;
                        }
                    }
                    if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(1).getMType())!=-1){
                        takeoff = true;
                        if(!trans.checkUnloadRange(new Location(x,y+1),1)&&!trans.checkUnloadRange(new Location(x,y-1),1)&&!trans.checkUnloadRange(new Location(x+1,y),1)&&!trans.checkUnloadRange(new Location(x-1,y),1)){
                            takeoff = false;
                        }
                    }
                }else if(trans.getUnitsCarried() == 1){
                    if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(1).getMType())!=-1){
                        takeoff = true;
                        if(!trans.checkUnloadRange(new Location(x,y+1),1)&&!trans.checkUnloadRange(new Location(x,y-1),1)&&!trans.checkUnloadRange(new Location(x+1,y),1)&&!trans.checkUnloadRange(new Location(x-1,y),1)){
                            takeoff = false;
                        }
                    }
                }
            }        if(u instanceof Transport && !u.isNoUnload()){
                Transport trans = (Transport) u;
                int x = trans.getLocation().getCol();
                int y = trans.getLocation().getRow();
                
                if(trans.getUnitsCarried() == 2){
                    if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(2).getMType())!=-1){
                        takeoff2 = true;
                        if(!trans.checkUnloadRange(new Location(x,y+1),2)&&!trans.checkUnloadRange(new Location(x,y-1),2)&&!trans.checkUnloadRange(new Location(x+1,y),2)&&!trans.checkUnloadRange(new Location(x-1,y),2)){
                            takeoff2 = false;
                        }
                    }
                    if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(1).getMType())!=-1){
                        takeoff = true;
                        if(!trans.checkUnloadRange(new Location(x,y+1),1)&&!trans.checkUnloadRange(new Location(x,y-1),1)&&!trans.checkUnloadRange(new Location(x+1,y),1)&&!trans.checkUnloadRange(new Location(x-1,y),1)){
                            takeoff = false;
                        }
                    }
                }else if(trans.getUnitsCarried() == 1){
                    if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(1).getMType())!=-1){
                        takeoff = true;
                        if(!trans.checkUnloadRange(new Location(x,y+1),1)&&!trans.checkUnloadRange(new Location(x,y-1),1)&&!trans.checkUnloadRange(new Location(x+1,y),1)&&!trans.checkUnloadRange(new Location(x-1,y),1)){
                            takeoff = false;
                        }
                    }
                }
            }
        }
        


        //UNLOAD
        if(u instanceof Transport && !u.isNoUnload() && u.getUnitType() != UnitID.CARRIER){
            Transport trans = (Transport) u;
            int x = trans.getLocation().getCol();
            int y = trans.getLocation().getRow();
            
            if(trans.getUnitsCarried() == 2){
                if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(2).getMType())!=-1){
                    unload2 = true;
                    if(!trans.checkUnloadRange(new Location(x,y+1),2)&&!trans.checkUnloadRange(new Location(x,y-1),2)&&!trans.checkUnloadRange(new Location(x+1,y),2)&&!trans.checkUnloadRange(new Location(x-1,y),2)){
                        unload2 = false;
                    }
                }
                if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(1).getMType())!=-1){
                    unload = true;
                    if(!trans.checkUnloadRange(new Location(x,y+1),1)&&!trans.checkUnloadRange(new Location(x,y-1),1)&&!trans.checkUnloadRange(new Location(x+1,y),1)&&!trans.checkUnloadRange(new Location(x-1,y),1)){
                        unload = false;
                    }
                }
            }else if(trans.getUnitsCarried() == 1){
                if(m.find(trans.getLocation()).getTerrain().moveCost(trans.getUnit(1).getMType())!=-1){
                    unload = true;
                    if(!trans.checkUnloadRange(new Location(x,y+1),1)&&!trans.checkUnloadRange(new Location(x,y-1),1)&&!trans.checkUnloadRange(new Location(x+1,y),1)&&!trans.checkUnloadRange(new Location(x-1,y),1)){
                        unload = false;
                    }
                }
            }
        }
        //conditions for takeoff have already been checked.
        if(u.getUType()==UnitID.CARRIER && !u.getMoved() && ((Carrier)u).getUnitsCarried() < 2)
            build = true;
        if(u.getUnitType() == UnitID.CARRIER && ((Carrier)u).usedAction())
        {
            fire = false;
            build = false;
        }
        if(u.getUnitType() == UnitID.CARRIER && ((Carrier)u).isBuiltUnit())
        {
            //don't disable takeoff unless the carrier has built a unit
            //it has been checked for moving however.
            takeoff = false;
        }
        if(secondunload || secondTakeoff){
            fire=false;
            //capture=false;
            //supply=false;
            repair=false;
            //launch=false;
            //explode=false;
            //dive=false;
            //rise=false;
            //hide=false;
            //appear=false;
        }
        //takeoff = false;
        //build = false;
        return new ContextMenu(u, fire,capture,supply,unload,unload2,repair,launch,explode,dive,rise,hide,appear,false,false,special1, special2, takeoff, takeoff2, build, screen);
    }
    
    //[NEW]
    public static ArrayList<Location> getContextTargs() {
        return contextTargs;
    }
}