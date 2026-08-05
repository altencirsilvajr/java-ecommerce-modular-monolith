import { CurrencyPipe } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnDestroy, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface Login { accessToken: string; role: string; }
interface Product { id: string; sku: string; name: string; price: number; active: boolean; }
interface Order { id: string; productName: string; quantity: number; total: number; status: string; failureReason?: string; }

@Component({selector:'app-root', imports:[FormsModule,CurrencyPipe], templateUrl:'./app.html', styleUrl:'./app.scss'})
export class App implements OnDestroy {
  private readonly http = inject(HttpClient); private poll?: ReturnType<typeof setInterval>;
  readonly products = signal<Product[]>([]); readonly order = signal<Order|null>(null); readonly error = signal(''); readonly busy = signal(false);
  email='customer@test.com'; password='Password123!'; token=''; selected=''; quantity=1;
  login():void { this.busy.set(true); this.error.set(''); this.http.post<Login>('/api/v1/users/login',{email:this.email,password:this.password}).subscribe({next:r=>{this.token=r.accessToken;this.busy.set(false);this.loadCatalog();},error:()=>{this.busy.set(false);this.error.set('Login recusado. Verifique a API e as credenciais.');}}); }
  loadCatalog():void { this.http.get<Product[]>('/api/v1/catalog/products').subscribe({next:p=>{this.products.set(p);if(!this.selected&&p.length)this.selected=p[0].id;},error:()=>this.error.set('Catalogo indisponivel.')}); }
  checkout():void { if(!this.selected)return;this.busy.set(true);this.error.set('');const headers=new HttpHeaders({Authorization:`Bearer ${this.token}`,'Idempotency-Key':crypto.randomUUID()});this.http.post<Order>('/api/v1/orders',{items:[{productId:this.selected,quantity:this.quantity}]},{headers}).subscribe({next:o=>{this.order.set(o);this.busy.set(false);this.pollOrder(o.id);},error:e=>{this.busy.set(false);this.error.set(e.error?.detail??'Checkout recusado.');}}); }
  private pollOrder(id:string):void { if(this.poll)clearInterval(this.poll);const load=()=>this.http.get<Order>(`/api/v1/orders/${id}`,{headers:new HttpHeaders({Authorization:`Bearer ${this.token}`})}).subscribe(o=>{this.order.set(o);if(['CONFIRMED','CANCELLED'].includes(o.status)&&this.poll){clearInterval(this.poll);this.poll=undefined;}});load();this.poll=setInterval(load,500); }
  ngOnDestroy():void { if(this.poll)clearInterval(this.poll); }
}
