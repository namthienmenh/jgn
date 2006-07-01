/*
 * Created on 26-jun-2006
 */

package com.captiveimagination.jgn.test.threaded;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import com.captiveimagination.jgn.JGN;
import com.captiveimagination.jgn.MessageClient;
import com.captiveimagination.jgn.MessageServer;
import com.captiveimagination.jgn.TCPMessageServer;
import com.captiveimagination.jgn.event.MessageAdapter;
import com.captiveimagination.jgn.message.Message;
import com.captiveimagination.jgn.test.basic.BasicMessage;

public class MultiThreadTest
{
   public static void main(String[] args) throws Exception
   {
      final MessageServer server = new TCPMessageServer(new InetSocketAddress(InetAddress.getLocalHost(), 1000));

      final int tasks = 32;
      final int threads = 8;
      final int messagesPerTask = 1000;

      server.addMessageListener(new MessageAdapter()
      {
         int counter = 0;

         public void messageReceived(Message message)
         {
            counter++;

            System.out.println("counter=" + counter + "/" + (tasks * messagesPerTask));
         }
      });

      launchUpdater(server);

      JGN.register(BasicMessage.class);

      //TaskDispatcher disp = new TaskDispatcher(threads);
      TaskDispatcher dispatcher = new TaskDispatcher(8);

      for (int i = 0; i < tasks; i++)
      {
         //disp.addTask(new Runnable()
    	  dispatcher.execute(new Runnable()
         {
            public void run()
            {
               try
               {
                  final MessageClient client = server.connectAndWait(new InetSocketAddress(InetAddress.getLocalHost(), 1000), 50000);
                  if (client == null)
                  {
                     System.err.println("client timeout");
                     return;
                  }
                  for (int i = 0; i < messagesPerTask; i++)
                  {
                     BasicMessage basic = new BasicMessage();
                     basic.setValue(i);
                     client.sendMessage(basic);
                  }
                  client.disconnect();
               }
               catch (Exception exc)
               {
                  exc.printStackTrace();
               }
            }
         });
      }

      //disp.waitForEmpty();
      //disp.shutdown();
   }

   private static final void launchUpdater(final MessageServer server)
   {
      Runnable task = new Runnable()
      {
         public void run()
         {
            while (true)
            {
               try
               {
                  Thread.sleep(1);
               }
               catch (Exception exc)
               {
                  exc.printStackTrace();
               }
               try
               {
                  server.update();
               }
               catch (IOException exc)
               {
                  exc.printStackTrace();
               }
            }
         }
      };

      Thread t = new Thread(task);
      t.setDaemon(true);
      t.start();
   }
}
