import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TelegramService {
  private get tg(): any {
    return (window as any).Telegram?.WebApp;
  }

  get initData(): string {
    return this.tg?.initData || '';
  }

  get isReady(): boolean {
    return !!this.tg;
  }

  ready() {
    this.tg?.ready();
    this.tg?.expand();
  }

  openTelegramLink(url: string) {
    this.tg?.openTelegramLink(url);
  }
}
